package com.io.usyc.Controller;

import com.io.usyc.Dto.*;
import com.io.usyc.Service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin - Usuarios", description = "Operaciones administrativas de usuarios (creación, cambio de contraseña, etc.)")
public class UserAdminController {

    private final UserAdminService service;

    public UserAdminController(UserAdminService service) {
        this.service = service;
    }

    @Operation(
            summary = "Crear usuario",
            description = "Crea un usuario en app_user, lo asocia a un plantel (cat_plantel) y asigna roles (app_user_role)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario creado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserCreateRes.class))),
            @ApiResponse(responseCode = "400", description = "Validación / datos inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {"message":"roleIds es requerido y debe traer al menos 1 rol."}
                            """))),
            @ApiResponse(responseCode = "404", description = "Plantel o Role no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {"message":"Plantel no encontrado: 3"}
                            """))),
            @ApiResponse(responseCode = "409", description = "Conflicto por duplicados (email/username/alumnoId)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {"message":"El username ya está registrado."}
                            """)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserCreateRes> create(
            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos del usuario a crear",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserCreateReq.class),
                            examples = @ExampleObject(name = "Ejemplo creación", value = """
                            {
                              "email": "alfred@usyc.com",
                              "username": "alfred",
                              "password": "MyS3cretPass!",
                              "fullName": "Alfred Pennyworth",
                              "plantelId": 3,
                              "roleIds": [1, 2]
                            }
                            """)
                    )
            )
            UserCreateReq req
    ) {
        return ResponseEntity.ok(service.createUser(req));
    }

    @Operation(
            summary = "Cambiar contraseña (por userId)",
            description = "Cambia la contraseña del usuario validando la contraseña actual. Recomendado protegerlo con rol ADMIN o equivalente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "Validación / contraseña actual inválida",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {"message":"La contraseña actual no es válida."}
                            """))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                            {"message":"Usuario no encontrado: 10"}
                            """)))
    })
    @PostMapping(
            value = "/{userId}/password",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> changePassword(
            @Parameter(
                    name = "userId",
                    in = ParameterIn.PATH,
                    description = "ID del usuario",
                    required = true,
                    example = "10"
            )
            @PathVariable Long userId,

            @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Contraseña actual y nueva",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PasswordChangeReq.class),
                            examples = @ExampleObject(name = "Ejemplo cambio", value = """
                            {
                              "newPassword": "NewPass123!"
                            }
                            """)
                    )
            )
            PasswordChangeReq req
    ) {
        service.changePassword(userId, req);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar usuarios",
            description = "Lista usuarios con filtros opcionales (plantel, activo, rol y búsqueda)."
    )
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<UserListItemRes>> list(
            @Parameter(description = "Filtrar por plantelId", example = "3")
            @RequestParam(required = false) Integer plantelId,

            @Parameter(description = "Filtrar por activo (true/false)", example = "true")
            @RequestParam(required = false) Boolean active,

            @Parameter(description = "Filtrar por código de rol (ej: ADMIN, USER)", example = "ADMIN")
            @RequestParam(required = false) String roleCode,

            @Parameter(description = "Búsqueda por username/email/fullName/alumnoId", example = "alfred")
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(service.listUsers(plantelId, active, roleCode, q));
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza datos del usuario (email/username/fullName/alumnoId/active/plantel). No cambia contraseña.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Validación/duplicado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario/Plantel no encontrado", content = @Content)
    })
    @PutMapping(value = "/update/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRes> update(
            @Parameter(example = "10") @PathVariable Long userId,
            @RequestBody UserUpdateReq req
    ) {
        return ResponseEntity.ok(service.updateUser(userId, req));
    }
}
