package de.uniks.stp.model;
import java.util.Objects;
import java.beans.PropertyChangeSupport;

public class Message
{
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_TIMESTAMP = "timestamp";
   public static final String PROPERTY_MESSAGE = "message";
   public static final String PROPERTY_SENDER = "sender";
   private String id;
   private int timestamp;
   private String message;
   private User sender;
   protected PropertyChangeSupport listeners;

   public String getId()
   {
      return this.id;
   }

   public Message setId(String value)
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

   public int getTimestamp()
   {
      return this.timestamp;
   }

   public Message setTimestamp(int value)
   {
      if (value == this.timestamp)
      {
         return this;
      }

      final int oldValue = this.timestamp;
      this.timestamp = value;
      this.firePropertyChange(PROPERTY_TIMESTAMP, oldValue, value);
      return this;
   }

   public String getMessage()
   {
      return this.message;
   }

   public Message setMessage(String value)
   {
      if (Objects.equals(value, this.message))
      {
         return this;
      }

      final String oldValue = this.message;
      this.message = value;
      this.firePropertyChange(PROPERTY_MESSAGE, oldValue, value);
      return this;
   }

   public User getSender()
   {
      return this.sender;
   }

   public Message setSender(User value)
   {
      if (this.sender == value)
      {
         return this;
      }

      final User oldValue = this.sender;
      if (this.sender != null)
      {
         this.sender = null;
         oldValue.withoutSentMessages(this);
      }
      this.sender = value;
      if (value != null)
      {
         value.withSentMessages(this);
      }
      this.firePropertyChange(PROPERTY_SENDER, oldValue, value);
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
      result.append(' ').append(this.getMessage());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.setSender(null);
   }
}
