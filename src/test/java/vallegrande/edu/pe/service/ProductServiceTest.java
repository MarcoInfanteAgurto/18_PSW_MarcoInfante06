package vallegrande.edu.pe.service;

import org.junit.jupiter.api.Test;
import vallegrande.edu.pe.model.Product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductServiceTest {

    private final ProductService productService = new ProductService();

    @Test
    void getProductsReturnsTheConfiguredCatalog() {
        assertEquals(5, productService.getProducts().size());
    }

    @Test
    void processProductHandlesValidEmptyAndNullNames() {
        assertEquals("Producto procesado: Laptop", productService.processProduct("Laptop"));
        assertEquals("Producto vacío", productService.processProduct(""));
        assertEquals("Producto no válido", productService.processProduct(null));
    }

    @Test
    void validateProductRequiresNameAndPositivePrice() {
        assertTrue(productService.validateProduct(new Product(1, "Laptop", 2500.0)));
        assertFalse(productService.validateProduct(new Product(1, "", 2500.0)));
        assertFalse(productService.validateProduct(new Product(1, "Laptop", 0.0)));
        assertFalse(productService.validateProduct(null));
    }
}
