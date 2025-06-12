package com.autobots.automanager.servicos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class CodigoBarrasGerador {
    
    private static final String PREFIXO_AUTOBOTS = "789";
    
    public String gerarCodigo() {
        StringBuilder codigo = new StringBuilder();
        
        codigo.append(PREFIXO_AUTOBOTS);
        
        LocalDateTime agora = LocalDateTime.now();
        String timestamp = agora.format(DateTimeFormatter.ofPattern("yyMMddHHss"));
        codigo.append(timestamp);
        
        Random random = new Random();
        codigo.append(String.format("%03d", random.nextInt(1000)));
        
        return codigo.toString();
    }
    
    public String gerarCodigoPorPerfil(String perfil) {
        String prefixoPerfil;
        
        switch (perfil.toUpperCase()) {
            case "CLIENTE":
                prefixoPerfil = "C";
                break;
            case "VENDEDOR":
                prefixoPerfil = "V";
                break;
            case "FORNECEDOR":
                prefixoPerfil = "R";
                break;
            case "ADMIN":
                prefixoPerfil = "A";
                break;
            case "GERENTE":
                prefixoPerfil = "G";
                break;
            default:
                prefixoPerfil = "U";
        }
        
        return prefixoPerfil + gerarCodigo();
    }
}