package edgruberman.bukkit.simpleregions.messaging;

import edgruberman.bukkit.simpleregions.messaging.messages.Confirmation;

public interface Recipients {

    public abstract Confirmation send(Message message);

}
