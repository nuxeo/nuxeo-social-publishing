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
 *      Andre Justo
 */

package org.nuxeo.ecm.social.publishing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 7.3
 */
@Name("wistiaPublishingActions")
@Scope(ScopeType.EVENT)
public class WistiaPublishingActions implements Serializable{

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(SocialPublishingActions.class);

    List<String> projects;

    /**
     * Helper to retrieve a list of projects for a given Wistia account
     * @param account
     * @return
     */
    public List<String> getProjects(String account) {

        if (account == null || account.length() == 0) {
            return null;
        }

        if (projects == null) {
            projects = new ArrayList<>();
            SocialMediaProvider service = getSocialPublishingService().getProvider("Wistia");
            projects = service.getProjects(account);
        }
        return projects;
    }

    private SocialPublishingService getSocialPublishingService() {
        return Framework.getService(SocialPublishingService.class);
    }
}
