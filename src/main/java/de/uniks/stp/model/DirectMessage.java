package de.uniks.stp.model;

public class DirectMessage extends Message
{
   public static final String PROPERTY_RECEIVER = "receiver";
   private User receiver;

   public User getReceiver()
   {
      return this.receiver;
   }

   public DirectMessage setReceiver(User value)
   {
      if (this.receiver == value)
      {
         return this;
      }

      final User oldValue = this.receiver;
      if (this.receiver != null)
      {
         this.receiver = null;
         oldValue.withoutReceivedMessages(this);
      }
      this.receiver = value;
      if (value != null)
      {
         value.withReceivedMessages(this);
      }
      this.firePropertyChange(PROPERTY_RECEIVER, oldValue, value);
      return this;
   }

   @Override
   public void removeYou()
   {
      super.removeYou();
      this.setReceiver(null);
   }
}
