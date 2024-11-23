# ¿Que hemos hecho durante esta seccion?

## Archivos modificados o creados
```
|-- src.main.java.com.eazybytes
|   |-- config
|   |   |-- ProjectSecurityConfig.java
|   |   |   |-- authenticationManager()
|   |--repository
|   |   |-- LoanRepository.java
|   |-- controller
|   |   |-- LoansController.java
```
### Antes de comenzar con Pre y Post
En la clase ProjectSecurityConfig debemos agregar la siguiente anotacion para habilitar el Pre y el Post 
```java
@EnableMethodSecurity(prePostEnabled = true)
public class ProjectSecurityConfig extends WebSecurityConfigurerAdapter {
    
}
```

### @PreAuthorize y @PostAuthorize
> Previamente en la clase ProjectSecurirityConfig hemos quitado el metodo que comprueba el rol del usuario en /myLoans
para así relaizar la siguiente demostración.

En la clase LoansRepository hemos agregado la anotacion @PreAuthorize. Esta anotacion se encarga de verificar si el usuario
tiene el rol USER. Si el usuario no tiene el rol USER, entonces no se puede acceder a la clase.
```java
@PreAuthorize("hasRole('USER')")
List<Loans> findByCustomerIdOrderByStartDtDesc(long customerId);
```

#### Diferencia entre @PreAuthorize y @PostAuthorize:
**PreAuthorize**
> se comprueba si el usuario tiene el rol USER antes de ejecutar la acción

**PostAuthorize**
> se comprueba si el usuario tiene el rol USER después de ejecutar la acción
---

### @PreFilter y @PostFilter
Descripcion: Los filtros Pre y post se utilizan para controlar los objetos que son recibidos o enviados por la aplicación.

**Ventajas de usar Pre**:
>Indicamos cuales son los objetos de una lista que permitimos que nuestra aplicacion reciba en base a las reglas de seguridad de la aplicación.

**Ventajas de usar Post**:
>Indicamos cuales son los objetos de la lista que devolvemos que permitimos que nuestra aplicacion envíe. De este modo nos aseguramos de evitar enviar
datos que no deseamos enviar por ejemplo a un usuario con rol USER.

### Ejemplo de implementación
En la clase ContactController hemos implementado el filtro **<u>Pre y Post</u>**

```java
import com.eazybytes.model.Contact;

import java.util.List;

/**
 * Estamos verificando que el objeto recibido o el que enviaremos, no contenga en el atributo 
 * contactName el valor "Test"
 * @param contacts Lista con el contacto que vamos a procesar
 * @return Contact contacto que hemos creado en la base de datos
 */
@PostMapping("/contact")
//@PreFilter("filterObject.contactName != 'Test'")
@PostFilter("filterObject.contactName != 'Test'")
public List<Contact> saveContactInquiryDetails(@RequestBody List<Contact> contacts) {
    List<Contact> returnContacs = new ArrayList<>();
    if (!contacts.isEmpty()) {
        Contact contact = contacts.getFirst();
        contact.setContactId(getServiceReqNumber());
        contact.setCreateDt(new Date(System.currentTimeMillis()));
        returnContacs.add(contactRepository.save(contact));
    }
    return returnContacs;
}
```