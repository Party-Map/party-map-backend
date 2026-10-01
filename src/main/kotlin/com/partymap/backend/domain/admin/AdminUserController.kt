package com.partymap.backend.domain.admin

import com.partymap.backend.config.Roles
import com.partymap.backend.domain.admin.dto.AdminUserDto
import com.partymap.backend.domain.admin.dto.AdminUserPageDto
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/** Platform administration of users: list and search them, grant and revoke the three manager roles. */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('${Roles.PARTYMAP_ADMIN}')")
class AdminUserController(private val adminUserService: AdminUserService) {
    @GetMapping
    fun list(
        @Parameter(description = "Part of the username, email, first or last name; blank lists everyone.")
        @RequestParam(required = false)
        q: String?,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) size: Int,
    ): AdminUserPageDto = adminUserService.list(q, page, size)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): AdminUserDto = adminUserService.get(id)

    @PutMapping("/{id}/roles/{role}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun grant(@PathVariable id: UUID, @PathVariable @ManagerRoleParameter role: String) =
        adminUserService.grant(id, role)

    @DeleteMapping("/{id}/roles/{role}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revoke(@PathVariable id: UUID, @PathVariable @ManagerRoleParameter role: String) =
        adminUserService.revoke(id, role)

    private companion object {
        /** Each user on a page costs a Keycloak call for their roles. */
        const val MAX_PAGE_SIZE = 50L
    }
}

/** Documents the `role` path variable as one of the grantable manager roles (the service enforces it). */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Parameter(
    description = "A manager role.",
    schema = Schema(allowableValues = [Roles.EVENT_ORGANIZER, Roles.PLACE_MANAGER, Roles.PERFORMER_MANAGER]),
)
annotation class ManagerRoleParameter
