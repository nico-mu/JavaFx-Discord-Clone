package de.uniks.stp.model;

public class ServerMessage extends Message
{
   public static final String PROPERTY_CHANNEL = "channel";
   private Channel channel;

   public Channel getChannel()
   {
      return this.channel;
   }

   public ServerMessage setChannel(Channel value)
   {
      if (this.channel == value)
      {
         return this;
      }

      final Channel oldValue = this.channel;
      if (this.channel != null)
      {
         this.channel = null;
         oldValue.withoutMessages(this);
      }
      this.channel = value;
      if (value != null)
      {
         value.withMessages(this);
      }
      this.firePropertyChange(PROPERTY_CHANNEL, oldValue, value);
      return this;
   }

   @Override
   public void removeYou()
   {
      super.removeYou();
      this.setChannel(null);
   }
}
