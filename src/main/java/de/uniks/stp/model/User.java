package de.uniks.stp.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class User
{
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_STATUS = "status";
   public static final String PROPERTY_RECEIVED_MESSAGES = "receivedMessages";
   public static final String PROPERTY_SENT_MESSAGES = "sentMessages";
   public static final String PROPERTY_AVAILABLE_SERVERS = "availableServers";
   public static final String PROPERTY_OWNED_SERVERS = "ownedServers";
   public static final String PROPERTY_ACCORD = "accord";
   private String id;
   private String name;
   private boolean status;
   private List<DirectMessage> receivedMessages;
   private List<Message> sentMessages;
   private List<Server> availableServers;
   private List<Server> ownedServers;
   private Accord accord;
   protected PropertyChangeSupport listeners;

   public String getId()
   {
      return this.id;
   }

   public User setId(String value)
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

   public User setName(String value)
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

   public boolean getStatus()
   {
      return this.status;
   }

   public User setStatus(boolean value)
   {
      if (value == this.status)
      {
         return this;
      }

      final boolean oldValue = this.status;
      this.status = value;
      this.firePropertyChange(PROPERTY_STATUS, oldValue, value);
      return this;
   }

   public List<DirectMessage> getReceivedMessages()
   {
      return this.receivedMessages != null ? Collections.unmodifiableList(this.receivedMessages) : Collections.emptyList();
   }

   public User withReceivedMessages(DirectMessage value)
   {
      if (this.receivedMessages == null)
      {
         this.receivedMessages = new ArrayList<>();
      }
      if (!this.receivedMessages.contains(value))
      {
         this.receivedMessages.add(value);
         value.setReceiver(this);
         this.firePropertyChange(PROPERTY_RECEIVED_MESSAGES, null, value);
      }
      return this;
   }

   public User withReceivedMessages(DirectMessage... value)
   {
      for (final DirectMessage item : value)
      {
         this.withReceivedMessages(item);
      }
      return this;
   }

   public User withReceivedMessages(Collection<? extends DirectMessage> value)
   {
      for (final DirectMessage item : value)
      {
         this.withReceivedMessages(item);
      }
      return this;
   }

   public User withoutReceivedMessages(DirectMessage value)
   {
      if (this.receivedMessages != null && this.receivedMessages.remove(value))
      {
         value.setReceiver(null);
         this.firePropertyChange(PROPERTY_RECEIVED_MESSAGES, value, null);
      }
      return this;
   }

   public User withoutReceivedMessages(DirectMessage... value)
   {
      for (final DirectMessage item : value)
      {
         this.withoutReceivedMessages(item);
      }
      return this;
   }

   public User withoutReceivedMessages(Collection<? extends DirectMessage> value)
   {
      for (final DirectMessage item : value)
      {
         this.withoutReceivedMessages(item);
      }
      return this;
   }

   public List<Message> getSentMessages()
   {
      return this.sentMessages != null ? Collections.unmodifiableList(this.sentMessages) : Collections.emptyList();
   }

   public User withSentMessages(Message value)
   {
      if (this.sentMessages == null)
      {
         this.sentMessages = new ArrayList<>();
      }
      if (!this.sentMessages.contains(value))
      {
         this.sentMessages.add(value);
         value.setSender(this);
         this.firePropertyChange(PROPERTY_SENT_MESSAGES, null, value);
      }
      return this;
   }

   public User withSentMessages(Message... value)
   {
      for (final Message item : value)
      {
         this.withSentMessages(item);
      }
      return this;
   }

   public User withSentMessages(Collection<? extends Message> value)
   {
      for (final Message item : value)
      {
         this.withSentMessages(item);
      }
      return this;
   }

   public User withoutSentMessages(Message value)
   {
      if (this.sentMessages != null && this.sentMessages.remove(value))
      {
         value.setSender(null);
         this.firePropertyChange(PROPERTY_SENT_MESSAGES, value, null);
      }
      return this;
   }

   public User withoutSentMessages(Message... value)
   {
      for (final Message item : value)
      {
         this.withoutSentMessages(item);
      }
      return this;
   }

   public User withoutSentMessages(Collection<? extends Message> value)
   {
      for (final Message item : value)
      {
         this.withoutSentMessages(item);
      }
      return this;
   }

   public List<Server> getAvailableServers()
   {
      return this.availableServers != null ? Collections.unmodifiableList(this.availableServers) : Collections.emptyList();
   }

   public User withAvailableServers(Server value)
   {
      if (this.availableServers == null)
      {
         this.availableServers = new ArrayList<>();
      }
      if (!this.availableServers.contains(value))
      {
         this.availableServers.add(value);
         value.withUsers(this);
         this.firePropertyChange(PROPERTY_AVAILABLE_SERVERS, null, value);
      }
      return this;
   }

   public User withAvailableServers(Server... value)
   {
      for (final Server item : value)
      {
         this.withAvailableServers(item);
      }
      return this;
   }

   public User withAvailableServers(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withAvailableServers(item);
      }
      return this;
   }

   public User withoutAvailableServers(Server value)
   {
      if (this.availableServers != null && this.availableServers.remove(value))
      {
         value.withoutUsers(this);
         this.firePropertyChange(PROPERTY_AVAILABLE_SERVERS, value, null);
      }
      return this;
   }

   public User withoutAvailableServers(Server... value)
   {
      for (final Server item : value)
      {
         this.withoutAvailableServers(item);
      }
      return this;
   }

   public User withoutAvailableServers(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withoutAvailableServers(item);
      }
      return this;
   }

   public List<Server> getOwnedServers()
   {
      return this.ownedServers != null ? Collections.unmodifiableList(this.ownedServers) : Collections.emptyList();
   }

   public User withOwnedServers(Server value)
   {
      if (this.ownedServers == null)
      {
         this.ownedServers = new ArrayList<>();
      }
      if (!this.ownedServers.contains(value))
      {
         this.ownedServers.add(value);
         value.setOwner(this);
         this.firePropertyChange(PROPERTY_OWNED_SERVERS, null, value);
      }
      return this;
   }

   public User withOwnedServers(Server... value)
   {
      for (final Server item : value)
      {
         this.withOwnedServers(item);
      }
      return this;
   }

   public User withOwnedServers(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withOwnedServers(item);
      }
      return this;
   }

   public User withoutOwnedServers(Server value)
   {
      if (this.ownedServers != null && this.ownedServers.remove(value))
      {
         value.setOwner(null);
         this.firePropertyChange(PROPERTY_OWNED_SERVERS, value, null);
      }
      return this;
   }

   public User withoutOwnedServers(Server... value)
   {
      for (final Server item : value)
      {
         this.withoutOwnedServers(item);
      }
      return this;
   }

   public User withoutOwnedServers(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withoutOwnedServers(item);
      }
      return this;
   }

   public Accord getAccord()
   {
      return this.accord;
   }

   public User setAccord(Accord value)
   {
      if (this.accord == value)
      {
         return this;
      }

      final Accord oldValue = this.accord;
      if (this.accord != null)
      {
         this.accord = null;
         oldValue.setCurrentUser(null);
      }
      this.accord = value;
      if (value != null)
      {
         value.setCurrentUser(this);
      }
      this.firePropertyChange(PROPERTY_ACCORD, oldValue, value);
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

   public boolean addPropertyChangeListener(PropertyChangeListener listener)
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      this.listeners.addPropertyChangeListener(listener);
      return true;
   }

   public boolean addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      this.listeners.addPropertyChangeListener(propertyName, listener);
      return true;
   }

   public boolean removePropertyChangeListener(PropertyChangeListener listener)
   {
      if (this.listeners != null)
      {
         this.listeners.removePropertyChangeListener(listener);
      }
      return true;
   }

   public boolean removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
   {
      if (this.listeners != null)
      {
         this.listeners.removePropertyChangeListener(propertyName, listener);
      }
      return true;
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
      this.withoutReceivedMessages(new ArrayList<>(this.getReceivedMessages()));
      this.withoutSentMessages(new ArrayList<>(this.getSentMessages()));
      this.withoutAvailableServers(new ArrayList<>(this.getAvailableServers()));
      this.withoutOwnedServers(new ArrayList<>(this.getOwnedServers()));
      this.setAccord(null);
   }
}
