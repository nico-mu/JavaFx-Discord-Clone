package de.uniks.stp.model;
import java.util.Objects;
import java.beans.PropertyChangeSupport;

public class Accord
{
   public static final String PROPERTY_LANGUAGE = "language";
   public static final String PROPERTY_USER_KEY = "userKey";
   public static final String PROPERTY_CURRENT_USER = "currentUser";
   private String language;
   private String userKey;
   private User currentUser;
   protected PropertyChangeSupport listeners;

   public String getLanguage()
   {
      return this.language;
   }

   public Accord setLanguage(String value)
   {
      if (Objects.equals(value, this.language))
      {
         return this;
      }

      final String oldValue = this.language;
      this.language = value;
      this.firePropertyChange(PROPERTY_LANGUAGE, oldValue, value);
      return this;
   }

   public String getUserKey()
   {
      return this.userKey;
   }

   public Accord setUserKey(String value)
   {
      if (Objects.equals(value, this.userKey))
      {
         return this;
      }

      final String oldValue = this.userKey;
      this.userKey = value;
      this.firePropertyChange(PROPERTY_USER_KEY, oldValue, value);
      return this;
   }

   public User getCurrentUser()
   {
      return this.currentUser;
   }

   public Accord setCurrentUser(User value)
   {
      if (this.currentUser == value)
      {
         return this;
      }

      final User oldValue = this.currentUser;
      if (this.currentUser != null)
      {
         this.currentUser = null;
         oldValue.setAccord(null);
      }
      this.currentUser = value;
      if (value != null)
      {
         value.setAccord(this);
      }
      this.firePropertyChange(PROPERTY_CURRENT_USER, oldValue, value);
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
      result.append(' ').append(this.getLanguage());
      result.append(' ').append(this.getUserKey());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.setCurrentUser(null);
   }
}
