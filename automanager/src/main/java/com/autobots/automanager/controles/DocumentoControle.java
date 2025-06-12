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
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.modelo.DocumentoAtualizador;
import com.autobots.automanager.modelo.DocumentoSelecionador;
import com.autobots.automanager.repositorios.ClienteRepositorio;
import com.autobots.automanager.repositorios.DocumentoRepositorio;

@RestController
@RequestMapping("/documento")
public class DocumentoControle {
    @Autowired
    private DocumentoRepositorio repositorio;
    @Autowired
    private DocumentoSelecionador selecionador;
    @Autowired
    private ClienteRepositorio clienteRepositorio;

    // ver documento especifico -> filtra por nivel de acesso
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @documentoControle.canGerenteAccessDocument(#id)) or " +
                  "(hasRole('VENDEDOR') and @documentoControle.canVendedorAccessDocument(#id)) or " +
                  "(hasRole('CLIENTE') and @documentoControle.canClienteAccessDocument(#id))")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Documento>> obterDocumento(@PathVariable long id) {
        List<Documento> documentos = repositorio.findAll();
        Documento documento = selecionador.selecionar(documentos, id);
        
        if (documento == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        EntityModel<Documento> documentoModel = EntityModel.of(documento);
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).obterDocumento(id)).withSelfRel());
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).atualizarDocumento(id, null)).withRel("update"));
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).excluirDocumento(id)).withRel("delete"));
        documentoModel.add(linkTo(DocumentoControle.class).withRel("documentos"));
        
        return new ResponseEntity<>(documentoModel, HttpStatus.OK);
    }

    // ver documentos -> filtra por nivel de acesso
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Documento>>> obterDocumentos() {
        List<Documento> documentos = repositorio.findAll();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            Long currentUserId = userDetails.getCliente().getId();
            
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
                documentos = documentos.stream()
                    .filter(doc -> isDocumentOwnedByUser(doc, currentUserId))
                    .collect(Collectors.toList());
                    
            } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VENDEDOR"))) {
                documentos = documentos.stream()
                    .filter(doc -> {
                        Cliente owner = findDocumentOwner(doc);
                        if (owner == null) return false;
                        
                        return owner.getId().equals(currentUserId) || 
                               (owner.getPerfil() != null && owner.getPerfil().name().equals("CLIENTE"));
                    })
                    .collect(Collectors.toList());
                    
            } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE"))) {
                documentos = documentos.stream()
                    .filter(doc -> {
                        Cliente owner = findDocumentOwner(doc);
                        if (owner == null) return false;
                        
                        return owner.getPerfil() != null && 
                               (owner.getPerfil().name().equals("GERENTE") ||
                                owner.getPerfil().name().equals("VENDEDOR") ||
                                owner.getPerfil().name().equals("CLIENTE"));
                    })
                    .collect(Collectors.toList());
            }
        }
        
        List<EntityModel<Documento>> documentoModels = documentos.stream()
            .map(documento -> {
                EntityModel<Documento> documentoModel = EntityModel.of(documento);
                documentoModel.add(linkTo(methodOn(DocumentoControle.class).obterDocumento(documento.getId())).withSelfRel());
                documentoModel.add(linkTo(methodOn(DocumentoControle.class).atualizarDocumento(documento.getId(), null)).withRel("update"));
                documentoModel.add(linkTo(methodOn(DocumentoControle.class).excluirDocumento(documento.getId())).withRel("delete"));
                return documentoModel;
            })
            .collect(Collectors.toList());
        
        CollectionModel<EntityModel<Documento>> collectionModel = CollectionModel.of(documentoModels);
        collectionModel.add(linkTo(DocumentoControle.class).withSelfRel());
        
        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    // criar documento para cliente especifico
    @PreAuthorize("hasRole('ADMIN') or " +
                  "hasRole('GERENTE') or " +
                  "(hasRole('VENDEDOR') and @documentoControle.canVendedorCreateDocument(#clienteId)) or " +
                  "(hasRole('CLIENTE') and #clienteId == authentication.principal.cliente.id)")
    @PostMapping("/{clienteId}")
    public ResponseEntity<EntityModel<Documento>> cadastrarDocumentoParaCliente(
            @PathVariable long clienteId, 
            @RequestBody Documento documento) {
        
        Cliente cliente = clienteRepositorio.findById(clienteId).orElse(null);
        if (cliente == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Documento documentoSalvo = repositorio.save(documento);
        
        cliente.addDocumento(documentoSalvo);
        clienteRepositorio.save(cliente);
        
        EntityModel<Documento> documentoModel = EntityModel.of(documentoSalvo);
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).obterDocumento(documentoSalvo.getId())).withSelfRel());
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).atualizarDocumento(documentoSalvo.getId(), null)).withRel("update"));
        documentoModel.add(linkTo(methodOn(DocumentoControle.class).excluirDocumento(documentoSalvo.getId())).withRel("delete"));
        documentoModel.add(linkTo(methodOn(ClienteControle.class).obterCliente(clienteId)).withRel("cliente"));
        documentoModel.add(linkTo(DocumentoControle.class).withRel("documentos"));
        
        return new ResponseEntity<>(documentoModel, HttpStatus.CREATED);
    }

    // editar documentos
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @documentoControle.canGerenteAccessDocument(#id)) or " +
                  "(hasRole('VENDEDOR') and @documentoControle.canVendedorAccessDocument(#id)) or " +
                  "(hasRole('CLIENTE') and @documentoControle.canClienteAccessDocument(#id))")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Documento>> atualizarDocumento(@PathVariable long id, @RequestBody Documento atualizacao) {
        if (repositorio.existsById(id)) {
            Documento documento = repositorio.getById(id);
            DocumentoAtualizador atualizador = new DocumentoAtualizador();
            atualizador.atualizar(documento, atualizacao);
            Documento documentoAtualizado = repositorio.save(documento);
            
            EntityModel<Documento> documentoModel = EntityModel.of(documentoAtualizado);
            documentoModel.add(linkTo(methodOn(DocumentoControle.class).obterDocumento(id)).withSelfRel());
            documentoModel.add(linkTo(methodOn(DocumentoControle.class).excluirDocumento(id)).withRel("delete"));
            documentoModel.add(linkTo(DocumentoControle.class).withRel("documentos"));
            
            return new ResponseEntity<>(documentoModel, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // deletar documentos - admin, gerente e vendedor (apenas de clientes)
    @PreAuthorize("hasRole('ADMIN') or " +
                  "(hasRole('GERENTE') and @documentoControle.canGerenteAccessDocument(#id)) or " +
                  "(hasRole('VENDEDOR') and @documentoControle.canVendedorDeleteDocument(#id))")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirDocumento(@PathVariable long id) {
        if (repositorio.existsById(id)) {
            try {
                Documento documento = repositorio.getById(id);
                
                List<Cliente> clientes = clienteRepositorio.findAll();
                for (Cliente cliente : clientes) {
                    if (cliente.getDocumentos().contains(documento)) {
                        cliente.getDocumentos().remove(documento);
                        clienteRepositorio.save(cliente);
                    }
                }
                
                repositorio.delete(documento);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (Exception e) {
                System.err.println("Error deleting documento: " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private Cliente findDocumentOwner(Documento documento) {
        List<Cliente> clientes = clienteRepositorio.findAll();
        for (Cliente cliente : clientes) {
            if (cliente.getDocumentos().contains(documento)) {
                return cliente;
            }
        }
        return null;
    }

    private boolean isDocumentOwnedByUser(Documento documento, Long userId) {
        Cliente owner = findDocumentOwner(documento);
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

    public boolean canGerenteAccessDocument(long documentId) {
        List<Documento> documentos = repositorio.findAll();
        Documento documento = selecionador.selecionar(documentos, documentId);
        if (documento == null) return false;
        
        Cliente owner = findDocumentOwner(documento);
        if (owner == null || owner.getPerfil() == null) return false;
        
        String perfil = owner.getPerfil().name();
        return perfil.equals("GERENTE") || perfil.equals("VENDEDOR") || perfil.equals("CLIENTE");
    }

    public boolean canVendedorAccessDocument(long documentId) {
        List<Documento> documentos = repositorio.findAll();
        Documento documento = selecionador.selecionar(documentos, documentId);
        if (documento == null) return false;
        
        Cliente owner = findDocumentOwner(documento);
        if (owner == null) return false;
        
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) return false;
        
        return owner.getId().equals(currentUserId) || 
               (owner.getPerfil() != null && owner.getPerfil().name().equals("CLIENTE"));
    }

    public boolean canClienteAccessDocument(long documentId) {
        List<Documento> documentos = repositorio.findAll();
        Documento documento = selecionador.selecionar(documentos, documentId);
        if (documento == null) return false;
        
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && isDocumentOwnedByUser(documento, currentUserId);
    }

    public boolean canVendedorCreateDocument(long clienteId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) return false;
        
        if (clienteId == currentUserId) return true;
        
        Cliente targetClient = clienteRepositorio.findById(clienteId).orElse(null);
        return targetClient != null && targetClient.getPerfil() != null && 
               targetClient.getPerfil().name().equals("CLIENTE");
    }

    public boolean canVendedorDeleteDocument(long documentId) {
        List<Documento> documentos = repositorio.findAll();
        Documento documento = selecionador.selecionar(documentos, documentId);
        if (documento == null) return false;
        
        Cliente owner = findDocumentOwner(documento);
        if (owner == null || owner.getPerfil() == null) return false;
        
   
        return owner.getPerfil().name().equals("CLIENTE");
    }
}