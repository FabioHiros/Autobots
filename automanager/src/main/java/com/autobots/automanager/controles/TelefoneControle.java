package com.autobots.automanager.controles;

import java.util.List;
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
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.modelo.TelefoneAtualizador;
import com.autobots.automanager.modelo.TelefoneSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.TelefoneRepositorio;

@RestController
@RequestMapping("/telefone")
public class TelefoneControle {
    @Autowired
    private TelefoneRepositorio repositorio;
    @Autowired
    private TelefoneSelecionador selecionador;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    // ver telefone especifico -> filtra por nivel de acesso
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @telefoneControle.canGerenteAccessTelefone(#id)) or " +
                  "(hasRole('VENDEDOR') and @telefoneControle.canVendedorAccessTelefone(#id)) or " +
                  "(hasRole('CLIENTE') and @telefoneControle.canClienteAccessTelefone(#id))")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Telefone>> obterTelefone(@PathVariable long id) {
        List<Telefone> telefones = repositorio.findAll();
        Telefone telefone = selecionador.selecionar(telefones, id);
        
        if (telefone == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        EntityModel<Telefone> telefoneModel = EntityModel.of(telefone);
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).obterTelefone(id)).withSelfRel());
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).atualizarTelefone(id, null)).withRel("update"));
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).excluirTelefone(id)).withRel("delete"));
        telefoneModel.add(linkTo(TelefoneControle.class).withRel("telefones"));
        
        return new ResponseEntity<>(telefoneModel, HttpStatus.OK);
    }

    // ver telefones -> filtra por nivel de acesso
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Telefone>>> obterTelefones() {
        List<Telefone> telefones = repositorio.findAll();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            Long currentUserId = userDetails.getCliente().getId();
            
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                telefones = telefones.stream()
                    .filter(tel -> isTelefoneOwnedByUser(tel, currentUserId))
                    .collect(Collectors.toList());
                    
            } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"))) {
                telefones = telefones.stream()
                    .filter(tel -> {
                        Cliente owner = findTelefoneOwner(tel);
                        if (owner == null) return false;
                        
                        return owner.getId().equals(currentUserId) || 
                               (owner.getPerfil() != null && owner.getPerfil().name().equals("CLIENTE"));
                    })
                    .collect(Collectors.toList());
                    
            } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"))) {
                telefones = telefones.stream()
                    .filter(tel -> {
                        Cliente owner = findTelefoneOwner(tel);
                        if (owner == null) return false;
                        
                        return owner.getPerfil() != null && 
                               (owner.getPerfil().name().equals("GERENTE") ||
                                owner.getPerfil().name().equals("VENDEDOR") ||
                                owner.getPerfil().name().equals("CLIENTE"));
                    })
                    .collect(Collectors.toList());
            }
        }
        
        List<EntityModel<Telefone>> telefoneModels = telefones.stream()
            .map(telefone -> {
                EntityModel<Telefone> telefoneModel = EntityModel.of(telefone);
                telefoneModel.add(linkTo(methodOn(TelefoneControle.class).obterTelefone(telefone.getId())).withSelfRel());
                telefoneModel.add(linkTo(methodOn(TelefoneControle.class).atualizarTelefone(telefone.getId(), null)).withRel("update"));
                telefoneModel.add(linkTo(methodOn(TelefoneControle.class).excluirTelefone(telefone.getId())).withRel("delete"));
                return telefoneModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Telefone>> collectionModel = CollectionModel.of(telefoneModels);
        collectionModel.add(linkTo(TelefoneControle.class).withSelfRel());
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // criar telefone para cliente especifico
    @PreAuthorize("hasRole('ADMIN') or " +
                  "hasRole('GERENTE') or " +
                  "(hasRole('VENDEDOR') and @telefoneControle.canVendedorCreateTelefone(#clienteId))")
    @PostMapping("/cliente/{clienteId}")
    public ResponseEntity<EntityModel<Telefone>> cadastrarTelefoneParaCliente(
            @PathVariable long clienteId, 
            @RequestBody Telefone telefone) {
        
        Cliente cliente = clienteRepositorio.findById(clienteId).orElse(null);
        if (cliente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Telefone telefoneSalvo = repositorio.save(telefone);
        
        cliente.addTelefone(telefoneSalvo);
        clienteRepositorio.save(cliente);
        
        EntityModel<Telefone> telefoneModel = EntityModel.of(telefoneSalvo);
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).obterTelefone(telefoneSalvo.getId())).withSelfRel());
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).atualizarTelefone(telefoneSalvo.getId(), null)).withRel("update"));
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).excluirTelefone(telefoneSalvo.getId())).withRel("delete"));
        telefoneModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(clienteId)).withRel("cliente"));
        telefoneModel.add(linkTo(TelefoneControle.class).withRel("telefones"));
        
        return new ResponseEntity<>(telefoneModel, HttpStatus.CREATED);
    }

    // criar telefone para empresa especifica
    @PreAuthorize("hasRole('ADMIN') or hasRole('GERENTE')")
    @PostMapping("/empresa/{empresaId}")
    public ResponseEntity<EntityModel<Telefone>> cadastrarTelefoneParaEmpresa(
            @PathVariable long empresaId, 
            @RequestBody Telefone telefone) {
        
        Empresa empresa = empresaRepositorio.findById(empresaId).orElse(null);
        if (empresa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Telefone telefoneSalvo = repositorio.save(telefone);
        
        empresa.getTelefones().add(telefoneSalvo);
        empresaRepositorio.save(empresa);
        
        EntityModel<Telefone> telefoneModel = EntityModel.of(telefoneSalvo);
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).obterTelefone(telefoneSalvo.getId())).withSelfRel());
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).atualizarTelefone(telefoneSalvo.getId(), null)).withRel("update"));
        telefoneModel.add(linkTo(methodOn(TelefoneControle.class).excluirTelefone(telefoneSalvo.getId())).withRel("delete"));
        telefoneModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresaId)).withRel("empresa"));
        telefoneModel.add(linkTo(TelefoneControle.class).withRel("telefones"));
        
        return new ResponseEntity<>(telefoneModel, HttpStatus.CREATED);
    }

    // editar telefones
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @telefoneControle.canGerenteAccessTelefone(#id)) or " +
                  "(hasRole('VENDEDOR') and @telefoneControle.canVendedorAccessTelefone(#id))")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Telefone>> atualizarTelefone(@PathVariable long id, @RequestBody Telefone atualizacao) {
        if (repositorio.existsById(id)) {
            Telefone telefone = repositorio.getById(id);
            TelefoneAtualizador atualizador = new TelefoneAtualizador();
            atualizador.atualizar(telefone, atualizacao);
            Telefone telefoneAtualizado = repositorio.save(telefone);
            
            EntityModel<Telefone> telefoneModel = EntityModel.of(telefoneAtualizado);
            telefoneModel.add(linkTo(methodOn(TelefoneControle.class).obterTelefone(id)).withSelfRel());
            telefoneModel.add(linkTo(methodOn(TelefoneControle.class).excluirTelefone(id)).withRel("delete"));
            telefoneModel.add(linkTo(TelefoneControle.class).withRel("telefones"));
            
            return new ResponseEntity<>(telefoneModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // deletar telefones - admin, gerente e vendedor (apenas de clientes)
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @telefoneControle.canGerenteAccessTelefone(#id)) or " +
                  "(hasRole('VENDEDOR') and @telefoneControle.canVendedorDeleteTelefone(#id))")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirTelefone(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            try {
                Telefone telefone = repositorio.getById(id);
                
              
                List<Cliente> clientes = clienteRepositorio.findAll();
                for (Cliente cliente : clientes) {
                    if (cliente.getTelefones().contains(telefone)) {
                        cliente.getTelefones().remove(telefone);
                        clienteRepositorio.save(cliente);
                    }
                }
                
               
                List<Empresa> empresas = empresaRepositorio.findAll();
                for (Empresa empresa : empresas) {
                    if (empresa.getTelefones().contains(telefone)) {
                        empresa.getTelefones().remove(telefone);
                        empresaRepositorio.save(empresa);
                    }
                }
                
                repositorio.delete(telefone);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (Exception e) {
                System.err.println("Error deleting telefone: " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private Cliente findTelefoneOwner(Telefone telefone) {
        List<Cliente> clientes = clienteRepositorio.findAll();
        for (Cliente cliente : clientes) {
            if (cliente.getTelefones().contains(telefone)) {
                return cliente;
            }
        }
        return null;
    }

    private Empresa findTelefoneEmpresaOwner(Telefone telefone) {
        List<Empresa> empresas = empresaRepositorio.findAll();
        for (Empresa empresa : empresas) {
            if (empresa.getTelefones().contains(telefone)) {
                return empresa;
            }
        }
        return null;
    }

    private boolean isTelefoneOwnedByUser(Telefone telefone, Long userId) {
        Cliente owner = findTelefoneOwner(telefone);
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

    public boolean canGerenteAccessTelefone(long telefoneId) {
        List<Telefone> telefones = repositorio.findAll();
        Telefone telefone = selecionador.selecionar(telefones, telefoneId);
        if (telefone == null) return false;
        
        Cliente owner = findTelefoneOwner(telefone);
        if (owner == null || owner.getPerfil() == null) return false;
        
        String perfil = owner.getPerfil().name();
        return perfil.equals("GERENTE") || perfil.equals("VENDEDOR") || perfil.equals("CLIENTE");
    }

    public boolean canVendedorAccessTelefone(long telefoneId) {
        List<Telefone> telefones = repositorio.findAll();
        Telefone telefone = selecionador.selecionar(telefones, telefoneId);
        if (telefone == null) return false;
        
        Cliente owner = findTelefoneOwner(telefone);
        if (owner == null) return false;
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) return false;
        
        return owner.getId().equals(currentUserId) || 
               (owner.getPerfil() != null && owner.getPerfil().name().equals("CLIENTE"));
    }

    public boolean canClienteAccessTelefone(long telefoneId) {
        List<Telefone> telefones = repositorio.findAll();
        Telefone telefone = selecionador.selecionar(telefones, telefoneId);
        if (telefone == null) return false;
        
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && isTelefoneOwnedByUser(telefone, currentUserId);
    }

    public boolean canVendedorCreateTelefone(long clienteId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) return false;
        
        if (clienteId == currentUserId) return true;
        
        Cliente targetClient = clienteRepositorio.findById(clienteId).orElse(null);
        return targetClient != null && targetClient.getPerfil() != null && 
               targetClient.getPerfil().name().equals("CLIENTE");
    }

    public boolean canVendedorDeleteTelefone(long telefoneId) {
        List<Telefone> telefones = repositorio.findAll();
        Telefone telefone = selecionador.selecionar(telefones, telefoneId);
        if (telefone == null) return false;
        
        Cliente owner = findTelefoneOwner(telefone);
        if (owner == null || owner.getPerfil() == null) return false;
        
       
        return owner.getPerfil().name().equals("CLIENTE");
    }
}