/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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

package org.nuxeo.ecm.media.publishing.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.google.api.services.youtube.model.VideoStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.media.publishing.OAuth2MediaPublishingProvider;
import org.nuxeo.ecm.media.publishing.adapter.PublishableMedia;
import org.nuxeo.ecm.media.publishing.upload.MediaPublishingProgressListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Youtube Media Publishing Provider Service
 *
 * @since 7.3
 */
public class YouTubeService extends OAuth2MediaPublishingProvider {
    private static final Log log = LogFactory.getLog(YouTubeService.class);

    public static final String PROVIDER = "YouTube";

    public YouTubeService() {
        super(PROVIDER);
    }

    public YouTubeClient getYouTubeClient(String account) throws ClientException {
        Credential credential = getCredential(account);
        return (credential != null && credential.getAccessToken() != null) ? new YouTubeClient(credential) : null;
    }

    @Override
    public String upload(PublishableMedia media, MediaPublishingProgressListener progressListener, String account, Map<String, String> options) throws IOException {

        MediaHttpUploaderProgressListener mediaUploaderListener = new MediaHttpUploaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpUploader uploader) throws IOException {
                switch (uploader.getUploadState()) {
                case INITIATION_STARTED:
                case INITIATION_COMPLETE:
                    progressListener.onStart();
                    break;
                case MEDIA_IN_PROGRESS:
                    progressListener.onProgress(uploader.getProgress());
                    break;
                case MEDIA_COMPLETE:
                    progressListener.onComplete();
                    break;
                case NOT_STARTED:
                    log.info("Upload Not Started!");
                    break;
                }
            }
        };

        Video youtubeVideo = new Video();

        VideoStatus status = new VideoStatus();
        String privacyStatus = options.get("privacyStatus");
        if (privacyStatus != null) {
            status.setPrivacyStatus(privacyStatus);
        } else {
            status.setPrivacyStatus("unlisted");
        }
        youtubeVideo.setStatus(status);

        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(media.getTitle());
        snippet.setDescription(media.getDescription());

        List<String> tags = new ArrayList<>();
        String inputTags = options.get("tags");
        if (inputTags != null) {
            tags.addAll(Arrays.asList(inputTags.split("\\s*,\\s*")));
        }
        snippet.setTags(tags);

        youtubeVideo.setSnippet(snippet);

        // upload original video
        Blob blob = media.getBlob();

        String mimeType = blob.getMimeType();
        long length = blob.getLength();
        youtubeVideo = getYouTubeClient(account).upload(youtubeVideo, blob.getStream(), mimeType, length, mediaUploaderListener);

        return youtubeVideo.getId();
    }

    @Override
    public String getPublishedUrl(String mediaId, String account) {
        return "https://www.youtube.com/watch?v=" + mediaId;
    }

    @Override
    public String getEmbedCode(String mediaId, String account) {
        return (mediaId == null) ? null : "https://www.youtube.com/embed/" + mediaId;
    }

    @Override
    public Map<String, String> getStats(String mediaId, String account) {
        YouTubeClient client = getYouTubeClient(account);
        if (client == null) {
            return Collections.emptyMap();
        }

        try {
            VideoStatistics stats = client.getStatistics(mediaId);
            if (stats == null) {
                return null;
            }

            Map<String, String> map = new HashMap<>();
            map.put("label.mediaPublishing.stats.views", stats.getViewCount().toString());
            map.put("label.mediaPublishing.stats.comments", stats.getCommentCount().toString());
            map.put("label.mediaPublishing.stats.likes", stats.getLikeCount().toString());
            map.put("label.mediaPublishing.stats.dislikes", stats.getDislikeCount().toString());
            map.put("label.mediaPublishing.stats.favorites", stats.getFavoriteCount().toString());
            return map;
        } catch (IOException e) {
            return null;
        }
    }
}
