package pl.futurecollars.invoicing.db.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.WithId;

public class InMemoryDatabase<T extends WithId> implements Database<T> {

  private final Map<Long, T> items = new HashMap<>();
  private long nextId = 1;

  @Override
  public long save(T item) {
    item.setId(nextId);
    items.put(nextId, item);

    return nextId++;
  }

  @Override
  public Optional<T> getById(long id) {
    return Optional.ofNullable(items.get(id));
  }

  @Override
  public List<T> getAll() {
    return new ArrayList<>(items.values());
  }

  @Override
  public Optional<T> update(long id, T updatedItem) {
    updatedItem.setId(id);

    return Optional.ofNullable(items.put(id, updatedItem));
  }

  @Override
  public Optional<T> delete(long id) {
    return Optional.ofNullable(items.remove(id));
  }
}
