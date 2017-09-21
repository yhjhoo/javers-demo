package me.prince.javersdemo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.repository.jql.QueryBuilder;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@SpringBootApplication
public class JaversDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JaversDemoApplication.class, args);
    }

}


@Configuration
@EnableSwagger2
class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

}

@Entity
@Data
@NoArgsConstructor
class Person {
    @Id
    String id;

    @DiffIgnore
    String name;
    String description;
}

//@Repository
//@RepositoryRestController()
//@RepositoryRestResource(path = "person")
@JaversSpringDataAuditable
interface PersonRepo extends CrudRepository<Person, String> {}


@RestController
@RequestMapping("/person")
class WelcomeController {
    PersonRepo personRepo;

    public WelcomeController(PersonRepo personRepo) {
        this.personRepo = personRepo;
    }

    @GetMapping("/")
    public Iterable<Person> hello() {
        return personRepo.findAll();
    }

    @GetMapping("/{id}")
    public Person hello(@PathVariable String id) {
        return personRepo.findOne(id);
    }


    @PutMapping("/")
    public Person hello(@RequestBody Person person) {
        return personRepo.save(person);
    }


}

@RestController
@RequestMapping(value = "/audit")
class AuditController {

    private final Javers javers;

    @Autowired
    public AuditController(Javers javers) {
        this.javers = javers;
    }

    @GetMapping("/person")
    public String getPersonChanges() {
        QueryBuilder jqlQuery = QueryBuilder.byClass(Person.class);

        List<Change> changes = javers.findChanges(jqlQuery.build());

        return javers.getJsonConverter().toJson(changes);
    }
}
