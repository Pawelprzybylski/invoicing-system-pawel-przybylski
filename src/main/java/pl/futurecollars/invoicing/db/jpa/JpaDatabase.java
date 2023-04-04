package pl.futurecollars.invoicing.db.jpa;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Invoice;
import pl.futurecollars.invoicing.model.WithId;

@AllArgsConstructor
public class JpaDatabase<T extends WithId> implements Database<T> {


  private final CrudRepository<T, Long> repository;


  @Override
  public long save(T item) {
    return repository.save(item).getId();
  }

  @Override
  public Optional<T> getById(long id) {
    return repository.findById(id);
  }

  @Override
  public List<T> getAll() {
    return StreamSupport
        .stream(repository.findAll().spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<T> update(long id, T updatedItem) {
    Optional<T> itemOptional = getById(id);

    if (itemOptional.isPresent()) {
      repository.save(updatedItem);
    }

    return itemOptional;
  }

  @Override
  public Optional<T> delete(long id) {
    Optional<T> item = getById(id);

    item.ifPresent(repository::delete);

    return item;
  }

}
