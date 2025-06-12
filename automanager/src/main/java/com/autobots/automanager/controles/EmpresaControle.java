package com.autobots.automanager.controles;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.autobots.automanager.entidades.Cliente;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.modelo.ClienteSelecionador;
import com.autobots.automanager.modelo.EmpresaAtualizador;
import com.autobots.automanager.modelo.EmpresaSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.EmpresaRepositorio;

@RestController
@RequestMapping("/empresa")
public class EmpresaControle {
    @Autowired
    private EmpresaRepositorio repositorio;
    @Autowired
    private EmpresaSelecionador selecionador;
    @Autowired
    private ClienteRepositorio clienteRepositorio;
    @Autowired
    private ClienteSelecionador clienteSelecionador;

    // ver empresa especifica
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Empresa>> obterEmpresa(@PathVariable long id) {
        List<Empresa> empresas = repositorio.findAll();
        Empresa empresa = selecionador.selecionar(empresas, id);
        
        if (empresa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        EntityModel<Empresa> empresaModel = EntityModel.of(empresa);
        empresaModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(id)).withSelfRel());
        empresaModel.add(linkTo(methodOn(EmpresaControle.class).atualizarEmpresa(id, new Empresa())).withRel("update"));
        empresaModel.add(linkTo(methodOn(EmpresaControle.class).excluirEmpresa(id)).withRel("delete"));
        empresaModel.add(linkTo(EmpresaControle.class).withRel("empresas"));
        
        if (empresa.getEndereco() != null) {
            empresaModel.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(empresa.getEndereco().getId())).withRel("endereco"));
        }
        
        if (!empresa.getUsuarios().isEmpty()) {
            empresaModel.add(linkTo(EmpresaControle.class).slash(id).slash("usuarios").withRel("usuarios"));
        }
        
        if (!empresa.getMercadorias().isEmpty()) {
            empresaModel.add(linkTo(EmpresaControle.class).slash(id).slash("mercadorias").withRel("mercadorias"));
        }
        
        if (!empresa.getServicos().isEmpty()) {
            empresaModel.add(linkTo(EmpresaControle.class).slash(id).slash("servicos").withRel("servicos"));
        }
        
        return new ResponseEntity<>(empresaModel, HttpStatus.OK);
    }

    // só adm consegue ver as empresas
    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Empresa>>> obterEmpresas() {
        List<Empresa> empresas = repositorio.findAll();
        
        List<EntityModel<Empresa>> empresaModels = empresas.stream()
            .map(empresa -> {
                EntityModel<Empresa> empresaModel = EntityModel.of(empresa);
                empresaModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresa.getId())).withSelfRel());
                empresaModel.add(linkTo(methodOn(EmpresaControle.class).atualizarEmpresa(empresa.getId(), new Empresa())).withRel("update"));
                empresaModel.add(linkTo(methodOn(EmpresaControle.class).excluirEmpresa(empresa.getId())).withRel("delete"));
                return empresaModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Empresa>> collectionModel = CollectionModel.of(empresaModels);
        collectionModel.add(linkTo(EmpresaControle.class).withSelfRel());
        collectionModel.add(linkTo(methodOn(EmpresaControle.class).cadastrarEmpresa(new Empresa())).withRel("create"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // criar empresa
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EntityModel<Empresa>> cadastrarEmpresa(@RequestBody Empresa empresa) {
        Empresa empresaSalva = repositorio.save(empresa);
        
        EntityModel<Empresa> empresaModel = EntityModel.of(empresaSalva);
        empresaModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresaSalva.getId())).withSelfRel());
        empresaModel.add(linkTo(methodOn(EmpresaControle.class).atualizarEmpresa(empresaSalva.getId(), new Empresa())).withRel("update"));
        empresaModel.add(linkTo(methodOn(EmpresaControle.class).excluirEmpresa(empresaSalva.getId())).withRel("delete"));
        empresaModel.add(linkTo(EmpresaControle.class).withRel("empresas"));
        
        return new ResponseEntity<>(empresaModel, HttpStatus.CREATED);
    }

    // atualizar empresa
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Empresa>> atualizarEmpresa(@PathVariable long id, @RequestBody Empresa atualizacao) {
        if (repositorio.existsById(id)) {
            Empresa empresa = repositorio.getById(id);
            EmpresaAtualizador atualizador = new EmpresaAtualizador();
            atualizador.atualizar(empresa, atualizacao);
            Empresa empresaAtualizada = repositorio.save(empresa);
            
            EntityModel<Empresa> empresaModel = EntityModel.of(empresaAtualizada);
            empresaModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(id)).withSelfRel());
            empresaModel.add(linkTo(methodOn(EmpresaControle.class).excluirEmpresa(id)).withRel("delete"));
            empresaModel.add(linkTo(EmpresaControle.class).withRel("empresas"));
            
            return new ResponseEntity<>(empresaModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // deletar empresa
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirEmpresa(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            try {
                Empresa empresa = repositorio.getById(id);
                repositorio.delete(empresa);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    // ver funcionarios de uma empresa
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    @GetMapping("/{id}/usuarios")
    public ResponseEntity<CollectionModel<EntityModel<Object>>> obterUsuariosEmpresa(@PathVariable long id) {
        List<Empresa> empresas = repositorio.findAll();
        Empresa empresa = selecionador.selecionar(empresas, id);
        
        if (empresa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<EntityModel<Object>> usuarioModels = empresa.getUsuarios().stream()
            .map(usuario -> EntityModel.of((Object) usuario)
                .add(linkTo(methodOn(ClienteControle.class).obterCliente(usuario.getId())).withSelfRel()))
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Object>> collectionModel = CollectionModel.of(usuarioModels);
        collectionModel.add(linkTo(methodOn(EmpresaControle.class).obterUsuariosEmpresa(id)).withSelfRel());
        collectionModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(id)).withRel("empresa"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }
    // ver servicos da emppresa
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE','VENDEDOR')")
    @GetMapping("/{id}/servicos")
    public ResponseEntity<CollectionModel<EntityModel<Object>>> obterServicosEmpresa(@PathVariable long id) {
        List<Empresa> empresas = repositorio.findAll();
        Empresa empresa = selecionador.selecionar(empresas, id);
        
        if (empresa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<EntityModel<Object>> servicoModels = empresa.getServicos().stream()
            .map(servico -> EntityModel.of((Object) servico)
                .add(linkTo(methodOn(ServicoControle.class).obterServico(servico.getId())).withSelfRel()))
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Object>> collectionModel = CollectionModel.of(servicoModels);
        collectionModel.add(linkTo(methodOn(EmpresaControle.class).obterServicosEmpresa(id)).withSelfRel());
        collectionModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(id)).withRel("empresa"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }
    //ver mercadorias da empresa
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','VENDEDOR')")
    @GetMapping("/{id}/mercadorias")
    public ResponseEntity<CollectionModel<EntityModel<Object>>> obterMercadoriasEmpresa(@PathVariable long id) {
        List<Empresa> empresas = repositorio.findAll();
        Empresa empresa = selecionador.selecionar(empresas, id);
        
        if (empresa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<EntityModel<Object>> mercadoriaModels = empresa.getMercadorias().stream()
            .map(mercadoria -> EntityModel.of((Object) mercadoria)
                .add(linkTo(methodOn(MercadoriaControle.class).obterMercadoria(mercadoria.getId())).withSelfRel()))
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Object>> collectionModel = CollectionModel.of(mercadoriaModels);
        collectionModel.add(linkTo(methodOn(EmpresaControle.class).obterMercadoriasEmpresa(id)).withSelfRel());
        collectionModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(id)).withRel("empresa"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }
    //ver vendas da empresa
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @GetMapping("/{id}/vendas")
    public ResponseEntity<CollectionModel<EntityModel<Object>>> obterVendasEmpresa(@PathVariable long id) {
        List<Empresa> empresas = repositorio.findAll();
        Empresa empresa = selecionador.selecionar(empresas, id);
        
        if (empresa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<EntityModel<Object>> vendaModels = empresa.getVendas().stream()
            .map(venda -> EntityModel.of((Object) venda)
                .add(linkTo(methodOn(VendaControle.class).obterVenda(venda.getId())).withSelfRel()))
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Object>> collectionModel = CollectionModel.of(vendaModels);
        collectionModel.add(linkTo(methodOn(EmpresaControle.class).obterVendasEmpresa(id)).withSelfRel());
        collectionModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(id)).withRel("empresa"));
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }
    // adicionar usuarios a uma empresa
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    @PostMapping("/{empresaId}/usuarios/{usuarioId}")
    public ResponseEntity<EntityModel<Cliente>> adicionarUsuarioEmpresa(@PathVariable long empresaId, @PathVariable long usuarioId) {
        List<Empresa> empresas = repositorio.findAll();
        Empresa empresa = selecionador.selecionar(empresas, empresaId);
        
        if (empresa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<Cliente> clientes = clienteRepositorio.findAll();
        Cliente usuario = clienteSelecionador.selecionar(clientes, usuarioId);
        
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        usuario.setEmpresa(empresa);
        Cliente usuarioAtualizado = clienteRepositorio.save(usuario);
        
        EntityModel<Cliente> usuarioModel = EntityModel.of(usuarioAtualizado);
        usuarioModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(usuarioId)).withSelfRel());
        usuarioModel.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresaId)).withRel("empresa"));
        
        return new ResponseEntity<>(usuarioModel, HttpStatus.OK);
    }
    //remover usuarios de uma empresa
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{empresaId}/usuarios/{usuarioId}")
    public ResponseEntity<Void> removerUsuarioEmpresa(@PathVariable long empresaId, @PathVariable long usuarioId) {
        List<Empresa> empresas = repositorio.findAll();
        Empresa empresa = selecionador.selecionar(empresas, empresaId);
        
        if (empresa == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<Cliente> clientes = clienteRepositorio.findAll();
        Cliente usuario = clienteSelecionador.selecionar(clientes, usuarioId);
        
        if (usuario == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        if (usuario.getEmpresa() == null || !usuario.getEmpresa().getId().equals(empresaId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        usuario.setEmpresa(null);
        clienteRepositorio.save(usuario);
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}