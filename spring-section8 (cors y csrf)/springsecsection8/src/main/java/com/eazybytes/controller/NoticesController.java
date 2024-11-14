package com.eazybytes.controller;

import com.eazybytes.model.Notice;
import com.eazybytes.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class NoticesController {

    private final NoticeRepository noticeRepository;
    /**
     * Endpoint para obtener una lista de notificaciones activas.
     *
     * Este método consulta el repositorio para obtener una lista de notificaciones activas y las
     * devuelve en la respuesta HTTP. La respuesta incluye una configuración de caché para
     * mejorar el rendimiento de la aplicación, reduciendo la carga del servidor y el tiempo de
     * respuesta para los usuarios.
     *
     * <p>La respuesta HTTP se configura con una cabecera {@code Cache-Control} utilizando
     * {@code .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))}. Esto establece un
     * límite de tiempo de almacenamiento en caché de 60 segundos, permitiendo a los clientes,
     * como navegadores o proxies, reutilizar la respuesta almacenada en caché durante ese
     * período de tiempo antes de hacer una nueva solicitud al servidor.
     *
     * <p>Ejemplo de la cabecera generada:
     * <pre>
     * Cache-Control: max-age=60
     * </pre>
     *
     * @return {@link ResponseEntity} que contiene una lista de notificaciones activas, con una
     *         configuración de caché que permite almacenar la respuesta durante 60 segundos.
     *         Si no hay notificaciones activas, se devuelve {@code null}.
     */
    @GetMapping("/notices")
    public ResponseEntity<List<Notice>> getNotices() {
        List<Notice> notices = noticeRepository.findAllActiveNotices();
        if (notices != null) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                    .body(notices);
        } else {
            return null;
        }
    }

}
