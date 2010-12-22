/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.platform.migration.aio.rest;

import java.io.ByteArrayInputStream;

import org.apache.commons.logging.Log;
import org.exoplatform.platform.migration.common.component.MarshallConfigurationService;
import org.exoplatform.platform.migration.common.constants.Constants;
import org.exoplatform.platform.migration.common.handler.ComponentHandler.Entry;
import org.exoplatform.platform.migration.common.handler.ComponentHandler.EntryType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.Response.Builder;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;

@URITemplate(Constants.CLASS_URI_TEMPLE)
public class MarshallConfigurationREST implements ResourceContainer {

  private MarshallConfigurationService marshallConfigurationService;
  
  private Log log = ExoLogger.getLogger(this.getClass());

  public MarshallConfigurationREST(MarshallConfigurationService marshallConfigurationService) {
    this.marshallConfigurationService = marshallConfigurationService;
  }

  @HTTPMethod("GET")
  @URITemplate()
  @OutputTransformer(StringOutputTransformer.class)
  public Response containersList() {
    log.info("Starting: " + this.getClass().getName());
    log.info("The Marshaller is ready for use ..");
    String htmlContainersLink;
    try {
      htmlContainersLink = marshallConfigurationService.generateHTMLContainersList();
    } catch (Exception e) {
      log.error("Error while generating containers list", e);
      return null;
    }
    return Response.Builder.ok().entity(htmlContainersLink, "text/html").build();
  }

  @HTTPMethod("GET")
  @URITemplate(Constants.GET_CONTAINERS_METHOD_URI_TEMPLE)
  @OutputTransformer(StringOutputTransformer.class)
  public Response componentsList(@QueryParam(Constants.CONTAINER_ID_PARAM_NAME) String containerId) {
    String htmlComponentsLink;
    try {
      htmlComponentsLink = marshallConfigurationService.generateHTMLComponentsList(containerId);
    } catch (Exception e) {
      log.error("Error while generating components list for container: " + containerId, e);
      return null;
    }
    return Response.Builder.ok().entity(htmlComponentsLink, "text/html").build();
  }

  @HTTPMethod("GET")
  @URITemplate(Constants.GET_COMPONENT_METHOD_URI_TEMPLE)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getComponentConfiguration(@QueryParam(Constants.CONTAINER_ID_PARAM_NAME) String containerId, @QueryParam(Constants.COMONENT_KEY_PARAM_NAME) String componentKey) {
    Entry configurationEntry;
    try {
      configurationEntry = marshallConfigurationService.getComponentConfiguration(containerId, componentKey);
    } catch (Exception e) {
      log.error("Error while generating component configuration for component: " + componentKey, e);
      return null;
    }
    Builder builder = Response.Builder.ok();
    if (configurationEntry.getType().equals(EntryType.ZIP)) {
      builder.header("Content-disposition", "attachment; filename=" + configurationEntry.getComponentName() + configurationEntry.getType());
    }
    return builder.entity(new ByteArrayInputStream(configurationEntry.getContent())).build();
  }

  @HTTPMethod("GET")
  @URITemplate(Constants.GET_CONTAINER_CONFIGURATION_URI_TEMPLE)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getAllComponentsConfiguration(@QueryParam(Constants.CONTAINER_ID_PARAM_NAME) String containerId) {
    Entry configurationEntry;
    try {
      configurationEntry = marshallConfigurationService.getAllComponentsConfiguration(containerId);
    } catch (Exception e) {
      log.error("Error while generating all configurations for container: " + containerId, e);
      return null;
    }
    Builder builder = Response.Builder.ok();
    builder.header("Content-disposition", "attachment; filename=" + configurationEntry.getComponentName() + configurationEntry.getType());
    return builder.entity(new ByteArrayInputStream(configurationEntry.getContent()), configurationEntry.getType().getMediaType()).build();
  }
}
