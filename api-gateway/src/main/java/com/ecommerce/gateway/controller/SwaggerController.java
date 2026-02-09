package com.ecommerce.gateway.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class SwaggerController {

    @Bean
    public RouterFunction<ServerResponse> swaggerUI() {
        return RouterFunctions.route(
                GET("/swagger-ui.html").or(GET("/swagger-ui/index.html")),
                request -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .bodyValue(getSwaggerUIHtml())
        );
    }

    private String getSwaggerUIHtml() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>API Gateway - Swagger UI</title>
                    <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui.css" />
                    <style>
                        html {
                            box-sizing: border-box;
                            overflow: -moz-scrollbars-vertical;
                            overflow-y: scroll;
                        }
                        *, *:before, *:after {
                            box-sizing: inherit;
                        }
                        body {
                            margin:0;
                            background: #fafafa;
                        }
                    </style>
                </head>
                <body>
                    <div id="swagger-ui"></div>
                    <script src="https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui-bundle.js"></script>
                    <script src="https://unpkg.com/swagger-ui-dist@5.9.0/swagger-ui-standalone-preset.js"></script>
                    <script>
                        window.onload = function() {
                            const ui = SwaggerUIBundle({
                                url: "/v3/api-docs",
                                dom_id: '#swagger-ui',
                                deepLinking: true,
                                presets: [
                                    SwaggerUIBundle.presets.apis,
                                    SwaggerUIStandalonePreset
                                ],
                                plugins: [
                                    SwaggerUIBundle.plugins.DownloadUrl
                                ],
                                layout: "StandaloneLayout",
                                validatorUrl: null,
                                tryItOutEnabled: true
                            });
                        };
                    </script>
                </body>
                </html>
                """;
    }
}
