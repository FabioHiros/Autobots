package com.autobots.automanager.entidades;

public enum PerfilUsuario {
    ADMIN,      // Full system administrator - can do all CRUD operations
    GERENTE,    // Manager - can do CRUD on users (gerente, vendedor, cliente), services, sales, merchandise  
    VENDEDOR,   // Salesperson - can do CRUD on cliente users, read services/merchandise, create own sales, read own sales
    FORNECEDOR, // Supplier
    CLIENTE     // Customer - can read own info and own sales
}