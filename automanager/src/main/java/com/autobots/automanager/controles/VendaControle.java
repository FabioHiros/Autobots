package com.autobots.automanager.controles;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
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
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.modelo.VendaAtualizador;
import com.autobots.automanager.modelo.VendaSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EmpresaRepositorio;
import com.autobots.automanager.repositorios.MercadoriaRepositorio;
import com.autobots.automanager.repositorios.ServicoRepositorio;
import com.autobots.automanager.repositorios.VendaRepositorio;

@RestController
@RequestMapping("/venda")
public class VendaControle {
    @Autowired
    private VendaRepositorio repositorio;
    @Autowired
    private VendaSelecionador selecionador;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private MercadoriaRepositorio mercadoriaRepositorio;
    @Autowired
    private ServicoRepositorio servicoRepositorio;
    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    // Force initialization of Hibernate proxies with debugging
    private void initializeVenda(Venda venda) {
        System.out.println("=== DEBUGGING VENDA INITIALIZATION ===");
        
        if (venda.getCliente() != null) {
            System.out.println("Cliente before init: " + venda.getCliente().getClass().getName());
            System.out.println("Cliente ID: " + venda.getCliente().getId());
            System.out.println("Cliente Nome before: " + venda.getCliente().getNome());
            Hibernate.initialize(venda.getCliente());
            System.out.println("Cliente Nome after: " + venda.getCliente().getNome());
        }
        
        if (venda.getFuncionario() != null) {
            System.out.println("Funcionario before init: " + venda.getFuncionario().getClass().getName());
            Hibernate.initialize(venda.getFuncionario());
            System.out.println("Funcionario Nome after: " + venda.getFuncionario().getNome());
        }
        
        if (venda.getMercadoria() != null) {
            System.out.println("Mercadoria before init: " + venda.getMercadoria().getClass().getName());
            System.out.println("Mercadoria Nome before: " + venda.getMercadoria().getNome());
            Hibernate.initialize(venda.getMercadoria());
            System.out.println("Mercadoria Nome after: " + venda.getMercadoria().getNome());
        }
        
        if (venda.getServico() != null) {
            System.out.println("Servico before init: " + venda.getServico().getClass().getName());
            Hibernate.initialize(venda.getServico());
            System.out.println("Servico Nome after: " + venda.getServico().getNome());
        }
        
        if (venda.getVeiculo() != null) {
            Hibernate.initialize(venda.getVeiculo());
        }
        
        if (venda.getEmpresa() != null) {
            System.out.println("Empresa before init: " + venda.getEmpresa().getClass().getName());
            Hibernate.initialize(venda.getEmpresa());
            System.out.println("Empresa RazaoSocial after: " + venda.getEmpresa().getRazaoSocial());
        }
        
        System.out.println("=== END DEBUGGING ===");
    }

    // ADMIN: pode ver todas
    // GERENTE: pode ver todas
    // VENDEDOR: pode ver suas prorpias vendas
    // CLIENTE: pode ver suas compras
    @PreAuthorize("hasRole('ADMIN') or hasRole('GERENTE') or " +
                  "(hasRole('VENDEDOR') and @vendaControle.isVendedorSale(#id)) or " +
                  "(hasRole('CLIENTE') and @vendaControle.isClienteSale(#id))")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<EntityModel<Venda>> obterVenda(@PathVariable long id) {
        List<Venda> vendas = repositorio.findAll();
        Venda venda = selecionador.selecionar(vendas, id);
        
        if (venda == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        // Force initialization of all related entities
        initializeVenda(venda);
        
        EntityModel<Venda> vendaModel = EntityModel.of(venda);
        vendaModel.add(linkTo(methodOn(VendaControle.class).obterVenda(id)).withSelfRel());
        vendaModel.add(linkTo(methodOn(VendaControle.class).atualizarVenda(id, null)).withRel("update"));
        vendaModel.add(linkTo(methodOn(VendaControle.class).excluirVenda(id)).withRel("delete"));
        vendaModel.add(linkTo(VendaControle.class).withRel("vendas"));
        
        return new ResponseEntity<>(vendaModel, HttpStatus.OK);
    }

    // ADMIN: pode ver todas
    // GERENTE: pode ver todas
    // VENDEDOR: pode ver suas prorpias vendas
    // CLIENTE: pode ver suas compras
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<CollectionModel<EntityModel<Venda>>> obterVendas() {
        List<Venda> vendas = repositorio.findAll();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"))) {
                vendas = vendas.stream()
                    .filter(v -> v.getFuncionario() != null && 
                                v.getFuncionario().getId().equals(userDetails.getCliente().getId()))
                    .collect(Collectors.toList());
            } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                vendas = vendas.stream()
                    .filter(v -> v.getCliente() != null && 
                                v.getCliente().getId().equals(userDetails.getCliente().getId()))
                    .collect(Collectors.toList());
            }
        }
        
        // Initialize all vendas
        vendas.forEach(this::initializeVenda);
        
        List<EntityModel<Venda>> vendaModels = vendas.stream()
            .map(venda -> {
                EntityModel<Venda> vendaModel = EntityModel.of(venda);
                vendaModel.add(linkTo(methodOn(VendaControle.class).obterVenda(venda.getId())).withSelfRel());
                return vendaModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Venda>> collectionModel = CollectionModel.of(vendaModels);
        collectionModel.add(linkTo(VendaControle.class).withSelfRel());
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // as 3 roles podem criar vendas
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR')")
    @PostMapping
    @Transactional
    public ResponseEntity<EntityModel<Venda>> cadastrarVenda(@RequestBody Venda venda) {
        if (venda.getCadastro() == null) {
            venda.setCadastro(new Date());
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"))) {
                venda.setFuncionario(userDetails.getCliente());
            }
        }
        
        // CRITICAL FIX: Load full entities before saving
        if (venda.getCliente() != null && venda.getCliente().getId() != null) {
            venda.setCliente(clienteRepositorio.findById(venda.getCliente().getId()).orElse(null));
        }
        if (venda.getMercadoria() != null && venda.getMercadoria().getId() != null) {
            venda.setMercadoria(mercadoriaRepositorio.findById(venda.getMercadoria().getId()).orElse(null));
        }
        if (venda.getServico() != null && venda.getServico().getId() != null) {
            venda.setServico(servicoRepositorio.findById(venda.getServico().getId()).orElse(null));
        }
        if (venda.getEmpresa() != null && venda.getEmpresa().getId() != null) {
            venda.setEmpresa(empresaRepositorio.findById(venda.getEmpresa().getId()).orElse(null));
        }
        
        Venda vendaSalva = repositorio.save(venda);
        
        EntityModel<Venda> vendaModel = EntityModel.of(vendaSalva);
        vendaModel.add(linkTo(methodOn(VendaControle.class).obterVenda(vendaSalva.getId())).withSelfRel());
        
        return new ResponseEntity<>(vendaModel, HttpStatus.CREATED);
    }

    // apenas admins e gerentes podem atualizar uma venda
    @PreAuthorize("hasRole('ADMIN') or hasRole('GERENTE')")
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<EntityModel<Venda>> atualizarVenda(@PathVariable long id, @RequestBody Venda atualizacao) {
        if (repositorio.existsById(id)) {
            Venda venda = repositorio.getById(id);
            VendaAtualizador atualizador = new VendaAtualizador();
            atualizador.atualizar(venda, atualizacao);
            Venda vendaAtualizada = repositorio.save(venda);
            
            // Refresh and initialize
            repositorio.flush();
            vendaAtualizada = repositorio.findById(vendaAtualizada.getId()).orElse(vendaAtualizada);
            initializeVenda(vendaAtualizada);
            
            EntityModel<Venda> vendaModel = EntityModel.of(vendaAtualizada);
            vendaModel.add(linkTo(methodOn(VendaControle.class).obterVenda(id)).withSelfRel());
            
            return new ResponseEntity<>(vendaModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // só gerentes e admin podem deletar vendas
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluirVenda(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            Venda venda = repositorio.getById(id);
            repositorio.delete(venda);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    public boolean isVendedorSale(long vendaId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            return false;
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Venda venda = repositorio.findById(vendaId).orElse(null);
        
        return venda != null && venda.getFuncionario() != null && 
               venda.getFuncionario().getId().equals(userDetails.getCliente().getId());
    }
    
    public boolean isClienteSale(long vendaId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            return false;
        }
        
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Venda venda = repositorio.findById(vendaId).orElse(null);
        
        return venda != null && venda.getCliente() != null && 
               venda.getCliente().getId().equals(userDetails.getCliente().getId());
    }
}