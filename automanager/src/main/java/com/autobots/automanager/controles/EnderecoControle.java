package com.autobots.automanager.controles;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.autobots.automanager.adaptadores.UserDetailsImpl;
import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.modelo.EnderecoAtualizador;
import com.autobots.automanager.modelo.EnderecoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.EnderecoRepositorio;

@RestController
@RequestMapping("/endereco")
public class EnderecoControle {
    @Autowired
    private EnderecoRepositorio repositorio;
    @Autowired
    private EnderecoSelecionador selecionador;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    // ver endereco especifico -> filtra por nivel de acesso
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @enderecoControle.canGerenteAccessEndereco(#id)) or " +
                  "(hasRole('VENDEDOR') and @enderecoControle.canVendedorAccessEndereco(#id)) or " +
                  "(hasRole('CLIENTE') and @enderecoControle.canClienteAccessEndereco(#id))")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Endereco>> obterEndereco(@PathVariable long id) {
        List<Endereco> enderecos = repositorio.findAll();
        Endereco endereco = selecionador.selecionar(enderecos, id);
        
        if (endereco == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        EntityModel<Endereco> enderecoModel = EntityModel.of(endereco);
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(id)).withSelfRel());
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).atualizarEndereco(id, null)).withRel("update"));
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).excluirEndereco(id)).withRel("delete"));
        enderecoModel.add(linkTo(EnderecoControle.class).withRel("enderecos"));
        
        return new ResponseEntity<>(enderecoModel, HttpStatus.OK);
    }

    // ver enderecos -> filtra por nivel de acesso
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Endereco>>> obterEnderecos() {
        List<Endereco> enderecos = repositorio.findAll();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            Long currentUserId = userDetails.getCliente().getId();
            
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                enderecos = enderecos.stream()
                    .filter(endereco -> isEnderecoOwnedByUser(endereco, currentUserId))
                    .collect(Collectors.toList());
                    
            } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"))) {
                enderecos = enderecos.stream()
                    .filter(endereco -> {
                        Cliente owner = findEnderecoOwner(endereco);
                        if (owner == null) return false;
                        
                        return owner.getId().equals(currentUserId) || 
                               (owner.getPerfil() != null && owner.getPerfil().name().equals("CLIENTE"));
                    })
                    .collect(Collectors.toList());
                    
            } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"))) {
                enderecos = enderecos.stream()
                    .filter(endereco -> {
                        Cliente owner = findEnderecoOwner(endereco);
                        if (owner == null) return false;
                        
                        return owner.getPerfil() != null && 
                               (owner.getPerfil().name().equals("GERENTE") ||
                                owner.getPerfil().name().equals("VENDEDOR") ||
                                owner.getPerfil().name().equals("CLIENTE"));
                    })
                    .collect(Collectors.toList());
            }
        }
        
        List<EntityModel<Endereco>> enderecoModels = enderecos.stream()
            .map(endereco -> {
                EntityModel<Endereco> enderecoModel = EntityModel.of(endereco);
                enderecoModel.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(endereco.getId())).withSelfRel());
                enderecoModel.add(linkTo(methodOn(EnderecoControle.class).atualizarEndereco(endereco.getId(), null)).withRel("update"));
                enderecoModel.add(linkTo(methodOn(EnderecoControle.class).excluirEndereco(endereco.getId())).withRel("delete"));
                return enderecoModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Endereco>> collectionModel = CollectionModel.of(enderecoModels);
        collectionModel.add(linkTo(EnderecoControle.class).withSelfRel());
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // FIXED: criar endereco para cliente especifico - removed invalid method references
    @PreAuthorize("hasRole('ADMIN') or " +
                  "hasRole('GERENTE') or " +
                  "(hasRole('VENDEDOR') and @enderecoControle.canVendedorCreateEndereco(#clienteId))")
    @PostMapping("/cliente/{clienteId}")
    public ResponseEntity<EntityModel<Endereco>> cadastrarEnderecoParaCliente(
            @PathVariable long clienteId, 
            @RequestBody Endereco endereco) {
        
        Optional<Cliente> clienteOpt = clienteRepositorio.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Cliente cliente = clienteOpt.get();
        
        // Check if cliente already has an endereco (since it's OneToOne)
        if (cliente.getEndereco() != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // 409 - Cliente already has an address
        }
        
        Endereco enderecoSalvo = repositorio.save(endereco);
        
        cliente.setEndereco(enderecoSalvo);
        clienteRepositorio.save(cliente);
        
        EntityModel<Endereco> enderecoModel = EntityModel.of(enderecoSalvo);
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(enderecoSalvo.getId())).withSelfRel());
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).atualizarEndereco(enderecoSalvo.getId(), null)).withRel("update"));
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).excluirEndereco(enderecoSalvo.getId())).withRel("delete"));
        enderecoModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(clienteId)).withRel("cliente"));
        enderecoModel.add(linkTo(EnderecoControle.class).withRel("enderecos"));
        
        return new ResponseEntity<>(enderecoModel, HttpStatus.CREATED);
    }

    // criar endereco para empresa especifica
    @PreAuthorize("hasRole('ADMIN') or hasRole('GERENTE')")
    @PostMapping("/empresa/{empresaId}")
    public ResponseEntity<EntityModel<Endereco>> cadastrarEnderecoParaEmpresa(
            @PathVariable long empresaId, 
            @RequestBody Endereco endereco) {
        
        Optional<Empresa> empresaOpt = empresaRepositorio.findById(empresaId);
        if (empresaOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Empresa empresa = empresaOpt.get();
        
        // Check if empresa already has an endereco (since it's OneToOne)
        if (empresa.getEndereco() != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // 409 - Company already has an address
        }
        
        Endereco enderecoSalvo = repositorio.save(endereco);
        
        empresa.setEndereco(enderecoSalvo);
        empresaRepositorio.save(empresa);
        
        EntityModel<Endereco> enderecoModel = EntityModel.of(enderecoSalvo);
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(enderecoSalvo.getId())).withSelfRel());
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).atualizarEndereco(enderecoSalvo.getId(), null)).withRel("update"));
        enderecoModel.add(linkTo(methodOn(EnderecoControle.class).excluirEndereco(enderecoSalvo.getId())).withRel("delete"));
        enderecoModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresaId)).withRel("empresa"));
        enderecoModel.add(linkTo(EnderecoControle.class).withRel("enderecos"));
        
        return new ResponseEntity<>(enderecoModel, HttpStatus.CREATED);
    }

    // editar endereco
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @enderecoControle.canGerenteAccessEndereco(#id)) or " +
                  "(hasRole('VENDEDOR') and @enderecoControle.canVendedorAccessEndereco(#id))")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Endereco>> atualizarEndereco(@PathVariable long id, @RequestBody Endereco atualizacao) {
        Optional<Endereco> enderecoOpt = repositorio.findById(id);
        if (enderecoOpt.isPresent()) {
            Endereco endereco = enderecoOpt.get();
            EnderecoAtualizador atualizador = new EnderecoAtualizador();
            atualizador.atualizar(endereco, atualizacao);
            Endereco enderecoAtualizado = repositorio.save(endereco);
            
            EntityModel<Endereco> enderecoModel = EntityModel.of(enderecoAtualizado);
            enderecoModel.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(id)).withSelfRel());
            enderecoModel.add(linkTo(methodOn(EnderecoControle.class).excluirEndereco(id)).withRel("delete"));
            enderecoModel.add(linkTo(EnderecoControle.class).withRel("enderecos"));
            
            return new ResponseEntity<>(enderecoModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @enderecoControle.canGerenteAccessEndereco(#id))")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirEndereco(@PathVariable long id) {
        Optional<Endereco> enderecoOpt = repositorio.findById(id);
        if (enderecoOpt.isPresent()) {
            try {
                Endereco endereco = enderecoOpt.get();
                
                // Remove references before deleting
                List<Cliente> clientes = clienteRepositorio.findAll();
                for (Cliente cliente : clientes) {
                    if (cliente.getEndereco() != null && cliente.getEndereco().getId().equals(id)) {
                        cliente.setEndereco(null);
                        clienteRepositorio.save(cliente);
                    }
                }
                
                List<Empresa> empresas = empresaRepositorio.findAll();
                for (Empresa empresa : empresas) {
                    if (empresa.getEndereco() != null && empresa.getEndereco().getId().equals(id)) {
                        empresa.setEndereco(null);
                        empresaRepositorio.save(empresa);
                    }
                }
                
                repositorio.delete(endereco);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (Exception e) {
                System.err.println("Error deleting endereco: " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Helper methods
    private Cliente findEnderecoOwner(Endereco endereco) {
        List<Cliente> clientes = clienteRepositorio.findAll();
        for (Cliente cliente : clientes) {
            if (cliente.getEndereco() != null && cliente.getEndereco().getId().equals(endereco.getId())) {
                return cliente;
            }
        }
        return null;
    }

    private Empresa findEnderecoEmpresaOwner(Endereco endereco) {
        List<Empresa> empresas = empresaRepositorio.findAll();
        for (Empresa empresa : empresas) {
            if (empresa.getEndereco() != null && empresa.getEndereco().getId().equals(endereco.getId())) {
                return empresa;
            }
        }
        return null;
    }

    private boolean isEnderecoOwnedByUser(Endereco endereco, Long userId) {
        Cliente owner = findEnderecoOwner(endereco);
        return owner != null && owner.getId().equals(userId);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            return userDetails.getCliente().getId();
        }
        return null;
    }

    // FIXED: Authorization methods with proper null checks and error handling
    public boolean canGerenteAccessEndereco(long enderecoId) {
        try {
            List<Endereco> enderecos = repositorio.findAll();
            Endereco endereco = selecionador.selecionar(enderecos, enderecoId);
            if (endereco == null) return false;
            
            Cliente owner = findEnderecoOwner(endereco);
            if (owner == null || owner.getPerfil() == null) {
                // If no cliente owner, check if it's owned by empresa (gerente can access empresa addresses)
                Empresa empresaOwner = findEnderecoEmpresaOwner(endereco);
                return empresaOwner != null;
            }
            
            String perfil = owner.getPerfil().name();
            return perfil.equals("GERENTE") || perfil.equals("VENDEDOR") || perfil.equals("CLIENTE");
        } catch (Exception e) {
            System.err.println("Error in canGerenteAccessEndereco: " + e.getMessage());
            return false;
        }
    }

    public boolean canVendedorAccessEndereco(long enderecoId) {
        try {
            List<Endereco> enderecos = repositorio.findAll();
            Endereco endereco = selecionador.selecionar(enderecos, enderecoId);
            if (endereco == null) return false;
            
            Cliente owner = findEnderecoOwner(endereco);
            if (owner == null) return false; // Vendedor cannot access empresa addresses
            
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) return false;
            
            // Vendedor can access their own address or cliente addresses
            return owner.getId().equals(currentUserId) || 
                   (owner.getPerfil() != null && owner.getPerfil().name().equals("CLIENTE"));
        } catch (Exception e) {
            System.err.println("Error in canVendedorAccessEndereco: " + e.getMessage());
            return false;
        }
    }

    public boolean canClienteAccessEndereco(long enderecoId) {
        try {
            List<Endereco> enderecos = repositorio.findAll();
            Endereco endereco = selecionador.selecionar(enderecos, enderecoId);
            if (endereco == null) return false;
            
            Long currentUserId = getCurrentUserId();
            return currentUserId != null && isEnderecoOwnedByUser(endereco, currentUserId);
        } catch (Exception e) {
            System.err.println("Error in canClienteAccessEndereco: " + e.getMessage());
            return false;
        }
    }

    // ADDED: Missing method that was referenced in PreAuthorize
    public boolean canVendedorCreateEndereco(long clienteId) {
        try {
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) return false;
            
            // Vendedor can create address for themselves
            if (clienteId == currentUserId) return true;
            
            // Vendedor can create address for clients
            Optional<Cliente> targetClientOpt = clienteRepositorio.findById(clienteId);
            if (targetClientOpt.isEmpty()) return false;
            
            Cliente targetClient = targetClientOpt.get();
            return targetClient.getPerfil() != null && 
                   targetClient.getPerfil().name().equals("CLIENTE");
        } catch (Exception e) {
            System.err.println("Error in canVendedorCreateEndereco: " + e.getMessage());
            return false;
        }
    }
}