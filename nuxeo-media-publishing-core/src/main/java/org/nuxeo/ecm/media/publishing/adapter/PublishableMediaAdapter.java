/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *      Nelson Silva
 */

package org.nuxeo.ecm.media.publishing.adapter;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.media.publishing.MediaPublishingProvider;
import org.nuxeo.ecm.media.publishing.MediaPublishingService;
import org.nuxeo.runtime.api.Framework;

import static org.nuxeo.ecm.media.publishing.MediaPublishingConstants.ACCOUNT_PROPERTY_NAME;
import static org.nuxeo.ecm.media.publishing.MediaPublishingConstants.ID_PROPERTY_NAME;
import static org.nuxeo.ecm.media.publishing.MediaPublishingConstants.PROVIDER_PROPERTY_NAME;
import static org.nuxeo.ecm.media.publishing.MediaPublishingConstants.PROVIDERS_PROPERTY_NAME;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @since 7.3
 */
public class PublishableMediaAdapter implements PublishableMedia {
    final DocumentModel doc;

    public PublishableMediaAdapter(DocumentModel doc) {
        this.doc = doc;
    }

    @Override
    public Map<String, Object> getProvider(String provider) {
        List<Map<String, Object>> providers = getProviders();
        for (Map<String, Object> entry : providers) {
            if (entry.get(PROVIDER_PROPERTY_NAME).equals(provider)) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public void putProvider(Map<String, Object> provider) {
        // Check if provider already exists
        // if so replace entry fields, otherwise add
        List<Map<String, Object>> providers = getProviders();
        boolean providerExists = false;
        for (Map<String, Object> entry : providers) {
            if (entry.get(PROVIDER_PROPERTY_NAME).equals(provider.get(PROVIDER_PROPERTY_NAME))) {
                entry.put(ID_PROPERTY_NAME, provider.get(ID_PROPERTY_NAME));
                entry.put(ACCOUNT_PROPERTY_NAME, provider.get(ACCOUNT_PROPERTY_NAME));
                providerExists = true;
                break;
            }
        }
        if (!providerExists) {
            providers.add(provider);
        }
        doc.setPropertyValue(PROVIDERS_PROPERTY_NAME, (Serializable) providers);
    }

    @Override
    public void removeProvider(String provider) {
        List<Map<String, Object>> providers = getProviders();
        Map<String, Object> providerEntry = getProvider(provider);
        providers.remove(providerEntry);
        setProviders(providers);
    }

    @Override
    public boolean isPublishedByProvider(String provider) {
        List<Map<String, Object>> providers = getProviders();
        for (Map<String, Object> entry : providers) {
            if (entry.get(PROVIDER_PROPERTY_NAME).equals(provider)) {
                String mediaId = (String) entry.get(ID_PROPERTY_NAME);
                String account = (String) entry.get(ACCOUNT_PROPERTY_NAME);
                if (getMediaPublishingProvider(provider).isMediaPublished(mediaId, account)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> getProviders() {
        return (List<Map<String, Object>>) doc.getPropertyValue(PROVIDERS_PROPERTY_NAME);
    }

    @Override
    public void setProviders(List<Map<String, Object>> providers) {
        doc.setPropertyValue(PROVIDERS_PROPERTY_NAME, (Serializable) providers);
    }

    @Override
    public String getId(String provider) {
        Map<String, Object> entry = getProvider(provider);
        if (entry == null) {
            return null;
        }
        return (String) entry.get(ID_PROPERTY_NAME);
    }

    @Override
    public String getAccount(String provider) {
        Map<String, Object> entry = getProvider(provider);
        if (entry == null) {
            return null;
        }
        return (String) entry.get(ACCOUNT_PROPERTY_NAME);
    }

    @Override
    public void setId(String id) {

    }

    @Override
    public String getTitle() {
        return doc.getTitle();
    }

    @Override
    public String getDescription() {
        return (String) doc.getPropertyValue("dc:description");
    }

    @Override
    public Blob getBlob() {
        return doc.getAdapter(BlobHolder.class).getBlob();
    }

    @Override
    public String getUrl(String provider) {

        return getMediaPublishingProvider(provider).getPublishedUrl(getId(provider), getAccount(provider));
    }

    @Override
    public String getEmbedCode(String provider) {
        return getMediaPublishingProvider(provider).getEmbedCode(getId(provider), getAccount(provider));
    }

    @Override
    public Map<String, String> getStats(String provider) {
        return getMediaPublishingProvider(provider).getStats(getId(provider), getAccount(provider));
    }

    private MediaPublishingProvider getMediaPublishingProvider(String provider) {
        return getMediaPublishingService().getProvider(provider);
    }

    private MediaPublishingService getMediaPublishingService() {
        return Framework.getService(MediaPublishingService.class);
    }
}
