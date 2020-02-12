package org.acme.person.model;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.acme.quickstart.NatsResource;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;

/**
 * PersonResource
 */
@Path("/person")
@ApplicationScoped
public class PersonResource {

    @Inject
    NatsResource natsResource;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> getAll() {
        return Person.listAll();
    }

    @GET
    @Path("/publish")
    @Produces(MediaType.TEXT_PLAIN)
    public String publish() {
        long start = System.nanoTime();
        natsResource.publish("test", "data".getBytes());
        long end = System.nanoTime();
        System.out.println((end - start) + " ns");
        return "ok";
    }

    @GET
    @Path("/eyes/{color}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> findByColor(@PathParam(value="color") EyeColor color) {
        return Person.findByColor(color);
    }

    @GET
    @Path("/datatable")
    @Produces(MediaType.APPLICATION_JSON)
    public DataTable datatable(
        @QueryParam(value="draw") int draw,
        @QueryParam(value="start") int start,
        @QueryParam(value="length") int length,
        @QueryParam(value="search[value]") String searchVal
    ) {
        DataTable result = new DataTable();
        result.setDraw(draw);

        PanacheQuery<Person> filteredPeople;
        if (searchVal != null && !searchVal.isEmpty()) {
            filteredPeople = Person.<Person>find("name like :search",
              Parameters.with("search", "%" + searchVal + "%"));
        } else {
            filteredPeople = Person.findAll();
        }

        int page_number = start / length;
        filteredPeople.page(page_number, length);

        result.setRecordsFiltered(filteredPeople.count());
        result.setData(filteredPeople.list());
        result.setRecordsTotal(Person.count());

        return result;  
    }
}
