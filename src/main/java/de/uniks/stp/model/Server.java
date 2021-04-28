package de.uniks.stp.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class Server
{
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_CATEGORIES = "categories";
   public static final String PROPERTY_USERS = "users";
   public static final String PROPERTY_OWNER = "owner";
   private String id;
   private String name;
   private List<Category> categories;
   private List<User> users;
   private User owner;
   protected PropertyChangeSupport listeners;

   public String getId()
   {
      return this.id;
   }

   public Server setId(String value)
   {
      if (Objects.equals(value, this.id))
      {
         return this;
      }

      final String oldValue = this.id;
      this.id = value;
      this.firePropertyChange(PROPERTY_ID, oldValue, value);
      return this;
   }

   public String getName()
   {
      return this.name;
   }

   public Server setName(String value)
   {
      if (Objects.equals(value, this.name))
      {
         return this;
      }

      final String oldValue = this.name;
      this.name = value;
      this.firePropertyChange(PROPERTY_NAME, oldValue, value);
      return this;
   }

   public List<Category> getCategories()
   {
      return this.categories != null ? Collections.unmodifiableList(this.categories) : Collections.emptyList();
   }

   public Server withCategories(Category value)
   {
      if (this.categories == null)
      {
         this.categories = new ArrayList<>();
      }
      if (!this.categories.contains(value))
      {
         this.categories.add(value);
         value.setServer(this);
         this.firePropertyChange(PROPERTY_CATEGORIES, null, value);
      }
      return this;
   }

   public Server withCategories(Category... value)
   {
      for (final Category item : value)
      {
         this.withCategories(item);
      }
      return this;
   }

   public Server withCategories(Collection<? extends Category> value)
   {
      for (final Category item : value)
      {
         this.withCategories(item);
      }
      return this;
   }

   public Server withoutCategories(Category value)
   {
      if (this.categories != null && this.categories.remove(value))
      {
         value.setServer(null);
         this.firePropertyChange(PROPERTY_CATEGORIES, value, null);
      }
      return this;
   }

   public Server withoutCategories(Category... value)
   {
      for (final Category item : value)
      {
         this.withoutCategories(item);
      }
      return this;
   }

   public Server withoutCategories(Collection<? extends Category> value)
   {
      for (final Category item : value)
      {
         this.withoutCategories(item);
      }
      return this;
   }

   public List<User> getUsers()
   {
      return this.users != null ? Collections.unmodifiableList(this.users) : Collections.emptyList();
   }

   public Server withUsers(User value)
   {
      if (this.users == null)
      {
         this.users = new ArrayList<>();
      }
      if (!this.users.contains(value))
      {
         this.users.add(value);
         value.withAvailableServers(this);
         this.firePropertyChange(PROPERTY_USERS, null, value);
      }
      return this;
   }

   public Server withUsers(User... value)
   {
      for (final User item : value)
      {
         this.withUsers(item);
      }
      return this;
   }

   public Server withUsers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withUsers(item);
      }
      return this;
   }

   public Server withoutUsers(User value)
   {
      if (this.users != null && this.users.remove(value))
      {
         value.withoutAvailableServers(this);
         this.firePropertyChange(PROPERTY_USERS, value, null);
      }
      return this;
   }

   public Server withoutUsers(User... value)
   {
      for (final User item : value)
      {
         this.withoutUsers(item);
      }
      return this;
   }

   public Server withoutUsers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withoutUsers(item);
      }
      return this;
   }

   public User getOwner()
   {
      return this.owner;
   }

   public Server setOwner(User value)
   {
      if (this.owner == value)
      {
         return this;
      }

      final User oldValue = this.owner;
      if (this.owner != null)
      {
         this.owner = null;
         oldValue.withoutOwnedServers(this);
      }
      this.owner = value;
      if (value != null)
      {
         value.withOwnedServers(this);
      }
      this.firePropertyChange(PROPERTY_OWNER, oldValue, value);
      return this;
   }

   public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      if (this.listeners != null)
      {
         this.listeners.firePropertyChange(propertyName, oldValue, newValue);
         return true;
      }
      return false;
   }

   public PropertyChangeSupport listeners()
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      return this.listeners;
   }

   @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder();
      result.append(' ').append(this.getId());
      result.append(' ').append(this.getName());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.withoutCategories(new ArrayList<>(this.getCategories()));
      this.withoutUsers(new ArrayList<>(this.getUsers()));
      this.setOwner(null);
   }
}
