import flexagon.ff.common.core.logging.FlexLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

public abstract class Request
{
  private static final String CLZ_NAM = Request.class.getName();
  private static final FlexLogger LOG = FlexLogger.getLogger(CLZ_NAM);

  public Request()
  {
    super();
  }

  public static Entity createFormData(List<FormDataBodyPart> formData)
  {
    final String methodName = "createFormData";
    LOG.logFinestEntering(methodName);

    FormDataMultiPart form = new FormDataMultiPart();

    for (FormDataBodyPart part : formData)
    {
      LOG.logFinest(methodName, "Creating form data for part: {0}", part.getName());
      if (part instanceof FileDataBodyPart)
      {
        form.field(part.getName(), part.getEntity(), part.getMediaType());

        //the content disposition file name seems to be lost in the step above, need to make sure its there.
        form.getField(part.getName()).setContentDisposition(FormDataContentDisposition.name(part.getName()).fileName(part.getContentDisposition().getFileName()).build());

      }
      else
      {
        form.field(part.getName(), part.getValue(), part.getMediaType());
      }

    }

    LOG.logFinestExiting(methodName);
    return Entity.entity(form, MediaType.MULTIPART_FORM_DATA);
  }

  public static Entity createFormData(FormDataBodyPart bodyPart)
  {
    List<FormDataBodyPart> list = new ArrayList<FormDataBodyPart>();
    list.add(bodyPart);

    return createFormData(list);
  }

  public abstract Map<String, Object> getQueryParams();

  public abstract String getResourceUri();

  /**
   * Only called for post/put/patch requests
   *
   * @return Entity object representing the body
   */
  public abstract Entity getBody();

  public abstract Map<String, Object> getHeaders();

}