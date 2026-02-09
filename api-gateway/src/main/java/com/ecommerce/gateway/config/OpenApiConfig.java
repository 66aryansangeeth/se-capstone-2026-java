package com.ecommerce.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Value("${GATEWAY_URL:http://localhost:8080}")
    private String gatewayUrl;

    @Bean
    public OpenAPI apiGatewayOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API Gateway - Unified API Documentation")
                        .description("Complete API documentation for all microservices: " +
                                "Authentication Service, Product Service, Order Service, and Payment Service. " +
                                "All endpoints are accessible through this gateway. " +
                                "Use the 'Authorize' button to add your JWT token for authenticated endpoints.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("E-Commerce Team")
                                .email("support@ecommerce.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addServersItem(new Server().url(gatewayUrl).description("API Gateway Server"))
                .components(createComponents())
                .paths(createAllPaths());

        return openAPI;
    }

    private Components createComponents() {
        Components components = new Components();

        // Security Scheme
        components.addSecuritySchemes("Bearer Authentication", createAPIKeyScheme());

        // Schema Definitions
        components.addSchemas("RegisterRequest", createRegisterRequestSchema());
        components.addSchemas("LoginRequest", createLoginRequestSchema());
        components.addSchemas("AuthResponse", createAuthResponseSchema());
        components.addSchemas("PasswordChangeRequest", createPasswordChangeRequestSchema());
        components.addSchemas("AdminPasswordChange", createAdminPasswordChangeSchema());
        components.addSchemas("UserResponse", createUserResponseSchema());
        components.addSchemas("UserDeleteRequest", createUserDeleteRequestSchema());

        components.addSchemas("ProductRequest", createProductRequestSchema());
        components.addSchemas("ProductResponse", createProductResponseSchema());
        components.addSchemas("SearchRequest", createSearchRequestSchema());

        components.addSchemas("OrderRequest", createOrderRequestSchema());
        components.addSchemas("OrderResponse", createOrderResponseSchema());
        components.addSchemas("OrderItemRequest", createOrderItemRequestSchema());
        components.addSchemas("OrderItemResponse", createOrderItemResponseSchema());

        components.addSchemas("PaymentRequest", createPaymentRequestSchema());

        return components;
    }

    private Paths createAllPaths() {
        Paths paths = new Paths();

        // Auth Service Endpoints
        paths.addPathItem("/api/auth/register", createAuthRegisterPath());
        paths.addPathItem("/api/auth/login", createAuthLoginPath());
        paths.addPathItem("/api/auth/change-password", createChangePasswordPath());
        paths.addPathItem("/api/auth/admin-reset", createAdminResetPath());
        paths.addPathItem("/api/auth/admin/users", createGetAllUsersPath());
        paths.addPathItem("/api/auth/admin/delete", createDeleteUserPath());

        // Product Service Endpoints
        paths.addPathItem("/api/products", createProductsPath());
        paths.addPathItem("/api/products/{id}", createProductByIdPath());
        paths.addPathItem("/api/products/search", createProductSearchPath());
        paths.addPathItem("/api/products/{id}/reduce-stock", createReduceStockPath());

        // Order Service Endpoints
        paths.addPathItem("/api/orders", createOrdersPath());
        paths.addPathItem("/api/orders/my-orders", createMyOrdersPath());
        paths.addPathItem("/api/orders/{id}", createOrderByIdPath());

        // Payment Service Endpoints
        paths.addPathItem("/api/payments/create-session", createPaymentSessionPath());

        return paths;
    }

    // ========== Auth Service Paths ==========

    private PathItem createAuthRegisterPath() {
        return new PathItem()
                .post(new Operation()
                        .tags(List.of("Authentication"))
                        .summary("Register a new user")
                        .description("Creates a new user account. Returns JWT token for authentication. Public endpoint.")
                        .requestBody(new RequestBody()
                                .required(true)
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(createSchemaRef("#/components/schemas/RegisterRequest")))))
                        .responses(new ApiResponses()
                                .addApiResponse("201", new ApiResponse()
                                        .description("User registered successfully")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(createSchemaRef("#/components/schemas/AuthResponse")))))
                                .addApiResponse("400", new ApiResponse().description("Invalid request or user already exists"))));
    }

    private PathItem createAuthLoginPath() {
        return new PathItem()
                .post(new Operation()
                        .tags(List.of("Authentication"))
                        .summary("User login")
                        .description("Authenticates a user and returns JWT token. Public endpoint.")
                        .requestBody(new RequestBody()
                                .required(true)
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(createSchemaRef("#/components/schemas/LoginRequest")))))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("Login successful")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(createSchemaRef("#/components/schemas/AuthResponse")))))
                                .addApiResponse("401", new ApiResponse().description("Invalid credentials"))));
    }

    private PathItem createChangePasswordPath() {
        return new PathItem()
                .put(new Operation()
                        .tags(List.of("Authentication"))
                        .summary("Change user password")
                        .description("Allows authenticated users to change their own password. Requires valid JWT token.")
                        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                        .requestBody(new RequestBody()
                                .required(true)
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(createSchemaRef("#/components/schemas/PasswordChangeRequest")))))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("Password updated successfully"))
                                .addApiResponse("401", new ApiResponse().description("Unauthorized"))
                                .addApiResponse("400", new ApiResponse().description("Incorrect old password"))));
    }

    private PathItem createAdminResetPath() {
        return new PathItem()
                .put(new Operation()
                        .tags(List.of("Authentication"))
                        .summary("Admin password reset")
                        .description("Allows admins to reset any user's password. Requires ADMIN role.")
                        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                        .requestBody(new RequestBody()
                                .required(true)
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(createSchemaRef("#/components/schemas/AdminPasswordChange")))))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("Password reset successfully"))
                                .addApiResponse("401", new ApiResponse().description("Unauthorized"))
                                .addApiResponse("403", new ApiResponse().description("Forbidden - Requires ADMIN role"))
                                .addApiResponse("404", new ApiResponse().description("User not found"))));
    }

    private PathItem createGetAllUsersPath() {
        return new PathItem()
                .get(new Operation()
                        .tags(List.of("Authentication"))
                        .summary("Get all users")
                        .description("Retrieves a list of all users in the system. Requires ADMIN role.")
                        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("Users retrieved successfully")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(new ArraySchema()
                                                                .items(createSchemaRef("#/components/schemas/UserResponse"))))))
                                .addApiResponse("401", new ApiResponse().description("Unauthorized"))
                                .addApiResponse("403", new ApiResponse().description("Forbidden - Requires ADMIN role"))));
    }

    private PathItem createDeleteUserPath() {
        return new PathItem()
                .delete(new Operation()
                        .tags(List.of("Authentication"))
                        .summary("Delete a user")
                        .description("Deletes a user by email. Requires ADMIN role.")
                        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                        .requestBody(new RequestBody()
                                .required(true)
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(createSchemaRef("#/components/schemas/UserDeleteRequest")))))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("User deleted successfully"))
                                .addApiResponse("401", new ApiResponse().description("Unauthorized"))
                                .addApiResponse("403", new ApiResponse().description("Forbidden - Requires ADMIN role"))
                                .addApiResponse("404", new ApiResponse().description("User not found"))));
    }

    // ========== Product Service Paths ==========

    private PathItem createProductsPath() {
        PathItem pathItem = new PathItem();

        // GET /api/products
        pathItem.setGet(new Operation()
                .tags(List.of("Product Management"))
                .summary("Get all products")
                .description("Retrieves all products. Optionally filter by category. Public endpoint.")
                .addParametersItem(new Parameter()
                        .name("category")
                        .in("query")
                        .required(false)
                        .schema(new StringSchema()))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("Products retrieved successfully")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(new ArraySchema()
                                                        .items(createSchemaRef("#/components/schemas/ProductResponse"))))))));

        // POST /api/products
        pathItem.setPost(new Operation()
                .tags(List.of("Product Management"))
                .summary("Create a new product")
                .description("Creates a new product. Requires ADMIN role. Price should be in cents (e.g., $10.50 = 1050).")
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .requestBody(new RequestBody()
                        .required(true)
                        .content(new Content()
                                .addMediaType("application/json", new MediaType()
                                        .schema(createSchemaRef("#/components/schemas/ProductRequest")))))
                .responses(new ApiResponses()
                        .addApiResponse("201", new ApiResponse().description("Product created successfully"))
                        .addApiResponse("401", new ApiResponse().description("Unauthorized"))
                        .addApiResponse("403", new ApiResponse().description("Forbidden - Requires ADMIN role"))));

        return pathItem;
    }

    private PathItem createProductByIdPath() {
        PathItem pathItem = new PathItem();

        // GET /api/products/{id}
        pathItem.setGet(new Operation()
                .tags(List.of("Product Management"))
                .summary("Get product by ID")
                .description("Retrieves a specific product by its ID. Public endpoint.")
                .addParametersItem(new Parameter()
                        .name("id")
                        .in("path")
                        .required(true)
                        .schema(new IntegerSchema().format("int64")))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("Product found")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(createSchemaRef("#/components/schemas/ProductResponse")))))
                        .addApiResponse("404", new ApiResponse().description("Product not found"))));

        // PUT /api/products/{id}
        pathItem.setPut(new Operation()
                .tags(List.of("Product Management"))
                .summary("Update a product")
                .description("Updates an existing product. Requires ADMIN role.")
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .addParametersItem(new Parameter()
                        .name("id")
                        .in("path")
                        .required(true)
                        .schema(new IntegerSchema().format("int64")))
                .requestBody(new RequestBody()
                        .required(true)
                        .content(new Content()
                                .addMediaType("application/json", new MediaType()
                                        .schema(createSchemaRef("#/components/schemas/ProductRequest")))))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse().description("Product updated successfully"))
                        .addApiResponse("401", new ApiResponse().description("Unauthorized"))
                        .addApiResponse("403", new ApiResponse().description("Forbidden - Requires ADMIN role"))
                        .addApiResponse("404", new ApiResponse().description("Product not found"))));

        // DELETE /api/products/{id}
        pathItem.setDelete(new Operation()
                .tags(List.of("Product Management"))
                .summary("Delete a product")
                .description("Deletes a product by ID. Requires ADMIN role.")
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .addParametersItem(new Parameter()
                        .name("id")
                        .in("path")
                        .required(true)
                        .schema(new IntegerSchema().format("int64")))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse().description("Product deleted successfully"))
                        .addApiResponse("401", new ApiResponse().description("Unauthorized"))
                        .addApiResponse("403", new ApiResponse().description("Forbidden - Requires ADMIN role"))
                        .addApiResponse("404", new ApiResponse().description("Product not found"))));

        return pathItem;
    }

    private PathItem createProductSearchPath() {
        return new PathItem()
                .post(new Operation()
                        .tags(List.of("Product Management"))
                        .summary("Search products")
                        .description("Search products by name (partial match) and/or category. " +
                                "Name search is case-insensitive partial match (e.g., 'ta' matches 'table'). " +
                                "Category search is exact match (case-insensitive). Public endpoint.")
                        .requestBody(new RequestBody()
                                .required(true)
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(createSchemaRef("#/components/schemas/SearchRequest")))))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("Search completed successfully")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(new ArraySchema()
                                                                .items(new Schema<>().$ref("#/components/schemas/ProductResponse"))))))));
    }

    private PathItem createReduceStockPath() {
        return new PathItem()
                .patch(new Operation()
                        .tags(List.of("Product Management"))
                        .summary("Reduce product stock")
                        .description("Reduces the stock quantity of a product. Used internally by order service. Public endpoint.")
                        .addParametersItem(new Parameter()
                                .name("id")
                                .in("path")
                                .required(true)
                                .schema(new IntegerSchema().format("int64")))
                        .addParametersItem(new Parameter()
                                .name("quantity")
                                .in("query")
                                .required(true)
                                .schema(new IntegerSchema()))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("Stock reduced successfully"))
                                .addApiResponse("400", new ApiResponse().description("Insufficient stock"))
                                .addApiResponse("404", new ApiResponse().description("Product not found"))));
    }

    // ========== Order Service Paths ==========

    private PathItem createOrdersPath() {
        return new PathItem()
                .post(new Operation()
                        .tags(List.of("Order Management"))
                        .summary("Place a new order")
                        .description("Creates a new order with the specified items. Validates products, checks stock, " +
                                "creates payment session, and returns order with Stripe checkout URL. Requires authentication.")
                        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                        .requestBody(new RequestBody()
                                .required(true)
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(createSchemaRef("#/components/schemas/OrderRequest")))))
                        .responses(new ApiResponses()
                                .addApiResponse("201", new ApiResponse()
                                        .description("Order created successfully")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(createSchemaRef("#/components/schemas/OrderResponse")))))
                                .addApiResponse("400", new ApiResponse().description("Invalid request or insufficient stock"))
                                .addApiResponse("401", new ApiResponse().description("Unauthorized"))));
    }

    private PathItem createMyOrdersPath() {
        return new PathItem()
                .get(new Operation()
                        .tags(List.of("Order Management"))
                        .summary("Get user's order history")
                        .description("Retrieves all orders for the authenticated user. Requires authentication.")
                        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("Orders retrieved successfully")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(new ArraySchema()
                                                                .items(new Schema<>().$ref("#/components/schemas/OrderResponse"))))))
                                .addApiResponse("401", new ApiResponse().description("Unauthorized"))));
    }

    private PathItem createOrderByIdPath() {
        return new PathItem()
                .get(new Operation()
                        .tags(List.of("Order Management"))
                        .summary("Get order by ID")
                        .description("Retrieves a specific order by ID. Requires ADMIN role.")
                        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                        .addParametersItem(new Parameter()
                                .name("id")
                                .in("path")
                                .required(true)
                                .schema(new IntegerSchema().format("int64")))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("Order found")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(createSchemaRef("#/components/schemas/OrderResponse")))))
                                .addApiResponse("401", new ApiResponse().description("Unauthorized"))
                                .addApiResponse("403", new ApiResponse().description("Forbidden - Requires ADMIN role"))
                                .addApiResponse("404", new ApiResponse().description("Order not found"))));
    }

    // ========== Payment Service Paths ==========

    private PathItem createPaymentSessionPath() {
        return new PathItem()
                .post(new Operation()
                        .tags(List.of("Payment Processing"))
                        .summary("Create Stripe checkout session")
                        .description("Creates a Stripe checkout session for an order. Returns the Stripe checkout URL. " +
                                "Amount should be in cents (e.g., $10.50 = 1050). Requires authentication.")
                        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                        .requestBody(new RequestBody()
                                .required(true)
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(createSchemaRef("#/components/schemas/PaymentRequest")))))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("Checkout session created successfully")
                                        .content(new Content()
                                                .addMediaType("application/json", new MediaType()
                                                        .schema(new StringSchema()
                                                                .example("https://checkout.stripe.com/pay/cs_test_...")))))
                                .addApiResponse("401", new ApiResponse().description("Unauthorized"))
                                .addApiResponse("400", new ApiResponse().description("Invalid request"))));
    }

    // ========== Schema Definitions ==========

    private Schema<?> createRegisterRequestSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("email", new StringSchema().description("User email address"));
        schema.addProperty("password", new StringSchema().description("User password (minimum 6 characters)"));
        schema.addProperty("role", new StringSchema().description("User role (USER or ADMIN)"));
        schema.setRequired(List.of("email", "password", "role"));
        return schema;
    }

    private Schema<?> createLoginRequestSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("email", new StringSchema().description("User email address"));
        schema.addProperty("password", new StringSchema().description("User password"));
        schema.setRequired(List.of("email", "password"));
        return schema;
    }

    private Schema<?> createAuthResponseSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("token", new StringSchema().description("JWT authentication token"));
        schema.addProperty("email", new StringSchema().description("User email"));
        schema.addProperty("role", new StringSchema().description("User role"));
        return schema;
    }

    private Schema<?> createPasswordChangeRequestSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("oldPassword", new StringSchema().description("Current password"));
        schema.addProperty("newPassword", new StringSchema().description("New password"));
        schema.setRequired(List.of("oldPassword", "newPassword"));
        return schema;
    }

    private Schema<?> createAdminPasswordChangeSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("email", new StringSchema().description("User email to reset password for"));
        schema.addProperty("newPassword", new StringSchema().description("New password"));
        schema.setRequired(List.of("email", "newPassword"));
        return schema;
    }

    private Schema<?> createUserResponseSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("id", new IntegerSchema().format("int64").description("User ID"));
        schema.addProperty("email", new StringSchema().description("User email"));
        schema.addProperty("role", new StringSchema().description("User role"));
        return schema;
    }

    private Schema<?> createUserDeleteRequestSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("email", new StringSchema().description("Email of user to delete"));
        schema.setRequired(List.of("email"));
        return schema;
    }

    private Schema<?> createProductRequestSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("name", new StringSchema().description("Product name"));
        schema.addProperty("description", new StringSchema().description("Product description"));
        schema.addProperty("price", new IntegerSchema().format("int64").description("Price in cents (e.g., $10.50 = 1050)"));
        schema.addProperty("stockQuantity", new IntegerSchema().description("Stock quantity"));
        schema.addProperty("category", new StringSchema().description("Product category"));
        schema.setRequired(List.of("name", "price", "stockQuantity", "category"));
        return schema;
    }

    private Schema<?> createProductResponseSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("id", new IntegerSchema().format("int64").description("Product ID"));
        schema.addProperty("name", new StringSchema().description("Product name"));
        schema.addProperty("description", new StringSchema().description("Product description"));
        schema.addProperty("price", new IntegerSchema().format("int64").description("Price in cents"));
        schema.addProperty("stockQuantity", new IntegerSchema().description("Stock quantity"));
        schema.addProperty("category", new StringSchema().description("Product category"));
        schema.addProperty("isAvailable", new BooleanSchema().description("Whether product is available"));
        return schema;
    }

    private Schema<?> createSearchRequestSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("name", new StringSchema().description("Product name (partial match, case-insensitive)"));
        schema.addProperty("category", new StringSchema().description("Product category (exact match, case-insensitive)"));
        return schema;
    }

    private Schema<?> createOrderRequestSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("items", new ArraySchema()
                .items(createSchemaRef("#/components/schemas/OrderItemRequest"))
                .description("List of order items"));
        schema.addProperty("stripePaymentIntentId", new StringSchema().description("Stripe payment intent ID (optional)"));
        schema.setRequired(List.of("items"));
        return schema;
    }

    private Schema<?> createOrderResponseSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("orderId", new IntegerSchema().format("int64").description("Order ID"));
        schema.addProperty("userEmail", new StringSchema().description("User email who placed the order"));
        schema.addProperty("items", new ArraySchema()
                .items(createSchemaRef("#/components/schemas/OrderItemResponse"))
                .description("List of order items"));
        schema.addProperty("totalAmount", new IntegerSchema().format("int64").description("Total amount in cents"));
        schema.addProperty("status", new StringSchema().description("Order status (PENDING, CONFIRMED, CANCELLED)"));
        schema.addProperty("orderDate", new StringSchema().format("date-time").description("Order creation date"));
        schema.addProperty("checkoutUrl", new StringSchema().description("Stripe checkout URL"));
        return schema;
    }

    private Schema<?> createOrderItemRequestSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("productId", new IntegerSchema().format("int64").description("Product ID"));
        schema.addProperty("quantity", new IntegerSchema().description("Quantity"));
        schema.setRequired(List.of("productId", "quantity"));
        return schema;
    }

    private Schema<?> createOrderItemResponseSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("productId", new IntegerSchema().format("int64").description("Product ID"));
        schema.addProperty("productName", new StringSchema().description("Product name"));
        schema.addProperty("quantity", new IntegerSchema().description("Quantity"));
        schema.addProperty("priceAtPurchase", new IntegerSchema().format("int64").description("Price at purchase time (cents)"));
        schema.addProperty("itemSubtotal", new IntegerSchema().format("int64").description("Item subtotal (cents)"));
        return schema;
    }

    private Schema<?> createPaymentRequestSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("orderId", new IntegerSchema().format("int64").description("Order ID"));
        schema.addProperty("amount", new IntegerSchema().format("int64").description("Amount in cents (e.g., $10.50 = 1050)"));
        schema.addProperty("customerEmail", new StringSchema().description("Customer email"));
        schema.addProperty("productName", new StringSchema().description("Product name"));
        schema.setRequired(List.of("orderId", "amount", "customerEmail", "productName"));
        return schema;
    }

    private Schema<?> createSchemaRef(String ref) {
        Schema<?> schema = new Schema<>();
        schema.set$ref(ref);
        return schema;
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
