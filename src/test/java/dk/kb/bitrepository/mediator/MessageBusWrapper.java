/*
 * #%L
 * Bitrepository Protocol
 *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package dk.kb.bitrepository.mediator;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;

import javax.jms.JMSException;
import java.util.List;

public class MessageBusWrapper implements MessageBus {
    private final MessageBus messageBus;

    public MessageBusWrapper(MessageBus messageBus) {
        super();
        this.messageBus = messageBus;
    }

    @Override
    public void sendMessage(Message content) {
        messageBus.sendMessage(content);
    }

    @Override
    public void addListener(String destinationId, MessageListener listener) {
        messageBus.addListener(destinationId, listener);
    }

    @Override
    public void addListener(String destinationId, MessageListener listener, boolean durable) {
        messageBus.addListener(destinationId, listener, durable);
    }

    @Override
    public void removeListener(String destinationId, MessageListener listener) {
        messageBus.removeListener(destinationId, listener);
    }

    @Override
    public void close() throws JMSException {
        messageBus.close();
    }

    @Override
    public void setComponentFilter(List<String> componentIDs) {
        messageBus.setComponentFilter(componentIDs);
    }

    @Override
    public void setCollectionFilter(List<String> collectionIDs) {
        messageBus.setCollectionFilter(collectionIDs);
    }
}
