package com.autobots.automanager.controles;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/")
public class RootControle {
    
    @GetMapping
    public ResponseEntity<ApiRoot> root() {
        ApiRoot root = new ApiRoot();
        
 
        root.add(linkTo(ClienteControle.class).withRel("clientes"));
        root.add(linkTo(EnderecoControle.class).withRel("enderecos"));
        root.add(linkTo(DocumentoControle.class).withRel("documentos"));
        root.add(linkTo(TelefoneControle.class).withRel("telefones"));
        
      
        root.add(linkTo(EmpresaControle.class).withRel("empresas"));
        root.add(linkTo(VeiculoControle.class).withRel("veiculos"));
        root.add(linkTo(MercadoriaControle.class).withRel("mercadorias"));
        root.add(linkTo(ServicoControle.class).withRel("servicos"));
        root.add(linkTo(VendaControle.class).withRel("vendas"));
        
      
        root.add(linkTo(methodOn(RootControle.class).root()).withSelfRel());
        
        return ResponseEntity.ok(root);
    }
    
    public static class ApiRoot extends RepresentationModel<ApiRoot> {
        private final String message = "AutoBots API - Sistema de Gestão Veicular";
        private final String description = "API RESTful para gestão completa de veículos, clientes, empresas, mercadorias, serviços e vendas";

        public String getMessage() {
            return message;
        }
        
        public String getDescription() {
            return description;
        }
 
    }
}