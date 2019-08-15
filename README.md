### Challenge 3

Building upon the previous challenges, we can provide a REST (ish) interface for our `Employee` database. Our
industrious client, would like the following endpoints:

```
GET /employees -> Return all employees
POST /employees -> Creates an employee
GET /employees/{id} -> Gets the employee with ID specified
PUT /employees/{id} -> Creates or replaces employee with ID
DELETE /employees/{id} -> Deletes the employee with ID
```

When writing the controller try to move any "business logic" code into its own bean (I tend to use
the pattern `XManager` for lack of imagination). This might be a little contrived for this example, 
but in general it helps testing (and the following section).

For example:
 
```
@Autowired
private EmployeeManager employeeManager;

@GetMapping("")
List<Employee> all() {
    return employeeManager.getAll();
}
```

For this challenge there will be less of a walk through, more of an independent struggle. There are a few 
pointers however. Firstly, a gotcha with regard to the previously created `Employee` entity. It will now
need to resemble something more like this:

```
@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
public class Employee {
    @Id
    @GeneratedValue
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private String role;
}
``` 

The addition of a `@RequiredArgsConstructor` is because of the need for a `@NoArgsConstructor`. `@Data` includes
the `@RequiredArgsConstructor`, but the way Spring JPA initialises entities means we need to include an empty
constructor too. In doing so, this negates the `@RequiredArgsConstructor` and the `final` behaviour on the 
fields. This has been worked around with the `@NonNull` annotation that will give us some security on undesirable
behaviours.

There is also a point around the `@GeneratedValue`. When saving an entity, any value in this annotated field
will be overridden. Therefore, for the `PUT` endpoint, do not expect new entities created with a specified `Id`
to honour the passed in value.


###### TIPS

As I'm not completely cruel, here as some helpers:

* `findById` returns an `Optional`, we will go into more details on these later, but you can use this to your
advantage with queries like: 
```
employeeRepository.findById(id)
                  .map(foundEmployee -> { // do some thing })
                  .orElseGet(() -> // return new Employee);           
or

employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
```

* Advisors can provide a means of neatly handling exceptions thrown out of controllers. Such and example may 
look like:

```
package demo.controllers.advice;

import demo.models.exceptions.EmployeeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class EmployeeControllerAdvice {

    @ResponseBody
    @ExceptionHandler(EmployeeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String employeeNotFoundHandler(EmployeeNotFoundException ex) {
        return ex.getMessage();
    }
}
```

* URL path variables can be acquired easily through an annotation, e.g.:

```
    @GetMapping("/{id}")
    Employee getEmployee(@PathVariable Long id) throws EmployeeNotFoundException { ... }
```

For now, do not worry about unit tests(!). Instead, run your application and try the following curls:

* `curl -v localhost:8080/employees`:
```
  *   Trying ::1...
  * TCP_NODELAY set
  * Connected to localhost (::1) port 8080 (#0)
  > GET /employees HTTP/1.1
  > Host: localhost:8080
  > User-Agent: curl/7.54.0
  > Accept: */*
  > 
  < HTTP/1.1 200 
  < Content-Type: application/json;charset=UTF-8
  < Transfer-Encoding: chunked
  < Date: Thu, 15 Aug 2019 15:58:42 GMT
  < 
  * Connection #0 to host localhost left intact
  [{"id":1,"name":"Bilbo Baggins","role":"burglar"},{"id":2,"name":"Frodo Baggins","role":"thief"}]
```

* `curl -v localhost:8080/employees/99`

```
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> GET /employees/99 HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.0
> Accept: */*
> 
< HTTP/1.1 404 
< Content-Type: text/plain;charset=UTF-8
< Content-Length: 29
< Date: Thu, 15 Aug 2019 16:01:38 GMT
< 
* Connection #0 to host localhost left intact
No Employee found with ID: 99
```

* `curl -v -X POST localhost:8080/employees -H 'Content-type:application/json' -d '{"name": "Samwise Gamgee", "role": "gardener"}'`
```
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> POST /employees HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.0
> Accept: */*
> Content-type:application/json
> Content-Length: 46
> 
* upload completely sent off: 46 out of 46 bytes
< HTTP/1.1 200 
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Thu, 15 Aug 2019 16:04:19 GMT
< 
* Connection #0 to host localhost left intact
{"id":3,"name":"Samwise Gamgee","role":"gardener"}
```

* `curl -X PUT localhost:8080/employees/3 -H 'Content-type:application/json' -d '{"name": "Samwise Gamgee", "role": "ring bearer"}'`

```
*   Trying ::1...
* TCP_NODELAY set
* Connected to localhost (::1) port 8080 (#0)
> PUT /employees/3 HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.54.0
> Accept: */*
> Content-type:application/json
> Content-Length: 49
> 
* upload completely sent off: 49 out of 49 bytes
< HTTP/1.1 200 
< Content-Type: application/json;charset=UTF-8
< Transfer-Encoding: chunked
< Date: Thu, 15 Aug 2019 16:06:44 GMT
< 
* Connection #0 to host localhost left intact
{"id":3,"name":"Samwise Gamgee","role":"ring bearer"}
```