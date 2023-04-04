package pl.futurecollars.invoicing.db;


import java.util.List;
import java.util.Objects;
import java.util.Optional;
import pl.futurecollars.invoicing.model.WithId;

public interface Database<T extends WithId> {
  long save(T item);

  Optional<T> getById(long id);

  List<T> getAll();

  Optional<T> update(long id, T updatedItem);

  Optional<T> delete(long id);

  default void reset() {
    getAll().forEach(item -> delete(item.getId()));
  }

}
