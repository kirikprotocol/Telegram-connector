package com.eyelinecom.whoisd.sads2.telegram.connector;

import com.eyelinecom.whoisd.sads2.Protocol;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSUrlUtils;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.connector.ChatCommand;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.events.Event;
import com.eyelinecom.whoisd.sads2.events.LinkEvent;
import com.eyelinecom.whoisd.sads2.events.MessageEvent.TextMessageEvent;
import com.eyelinecom.whoisd.sads2.exception.NotFoundResourceException;
import com.eyelinecom.whoisd.sads2.executors.connector.AbstractHTTPPushConnector;
import com.eyelinecom.whoisd.sads2.executors.connector.ProfileEnabledMessageConnector;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSExecutor;
import com.eyelinecom.whoisd.sads2.input.AbstractInputType;
import com.eyelinecom.whoisd.sads2.input.InputContact;
import com.eyelinecom.whoisd.sads2.input.InputFile;
import com.eyelinecom.whoisd.sads2.input.InputLocation;
import com.eyelinecom.whoisd.sads2.profile.Profile;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.session.SessionManager;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.internal.InlineCallbackQuery;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendChatAction;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Audio;
import com.eyelinecom.whoisd.sads2.telegram.api.types.CallbackQuery;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Contact;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Location;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Message;
import com.eyelinecom.whoisd.sads2.telegram.api.types.PhotoSize;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Sticker;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Update;
import com.eyelinecom.whoisd.sads2.telegram.api.types.User;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Video;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Voice;
import com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi;
import com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils;
import com.eyelinecom.whoisd.sads2.utils.ConnectorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.Protocol.TELEGRAM;
import static com.eyelinecom.whoisd.sads2.connector.ChatCommand.CLEAR_PROFILE;
import static com.eyelinecom.whoisd.sads2.connector.ChatCommand.INVALIDATE_SESSION;
import static com.eyelinecom.whoisd.sads2.connector.ChatCommand.SHOW_PROFILE;
import static com.eyelinecom.whoisd.sads2.connector.ChatCommand.WHO_IS;
import static com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils.parse;
import static com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils.unmarshal;
import static com.eyelinecom.whoisd.sads2.wstorage.profile.QueryRestrictions.property;

public class TelegramMessageConnector extends HttpServlet {

  private final static Log log = new Log4JLogger(Logger.getLogger(TelegramMessageConnector.class));

  private TelegramMessageConnectorImpl connector;

  @Override
  public void destroy() {
    super.destroy();
    connector.destroy();
  }

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    connector = new TelegramMessageConnectorImpl();

    try {
      final Properties properties = AbstractHTTPPushConnector.buildProperties(servletConfig);
      connector.init(properties);

    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  @Override
  protected void service(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

    final TelegramWebhookRequest request = new TelegramWebhookRequest(req);

    final SADSResponse response = connector.process(request);
    ConnectorUtils.fillHttpResponse(resp, response);
  }


  //
  //
  //

  private class TelegramMessageConnectorImpl
      extends ProfileEnabledMessageConnector<TelegramWebhookRequest> {

    /**
     * Response to a WebHook update.
     */
    @Override
    protected SADSResponse buildQueuedResponse(TelegramWebhookRequest req,
                                               SADSRequest sadsRequest) {
      try {
        final Update update = req.asUpdate();
        if (update.getMessage() != null) {
          return buildWebhookResponse(
              200,
              new SendChatAction(
                  req.getChatId(),
                  SendChatAction.ChatAction.TYPING).marshalAsWebHookResponse()
          );

        } else {
          // Do not send "typing" event in case this update is initiated by an inline keyboard
          // as it displays a spinner itself.
          return buildWebhookResponse(200);
        }

      } catch (TelegramApiException e) {
        getLog(req).error(e.getMessage(), e);
        return buildQueueErrorResponse(e, req, sadsRequest);
      } catch (IOException e) {
        getLog(req).error(e.getMessage(), e);
        return buildQueueErrorResponse(e, req, sadsRequest);
      }
    }

    @Override
    protected SADSResponse buildQueueErrorResponse(Exception e,
                                                   TelegramWebhookRequest httpServletRequest,
                                                   SADSRequest sadsRequest) {
      return buildWebhookResponse(500);
    }

    @Override
    protected Log getLogger() {
      return TelegramMessageConnector.log;
    }

    @Override
    protected String getSubscriberId(TelegramWebhookRequest req) throws Exception {
      if (req.getProfile() != null) {
        return req.getProfile().getWnumber();
      }

      final String token = req.getServiceToken();
      final Update update = req.asUpdate();
      final String incoming = req.getMessageText();

      if (ChatCommand.match(getServiceId(req), incoming, TELEGRAM) == CLEAR_PROFILE) {
        // Reset profile of the current user.
        final String userId = String.valueOf(update.getMessage().getFrom().getId());
        final Profile profile = getProfileStorage()
            .query()
            .where(property("telegram", "id").eq(userId))
            .get();
        if (profile != null) {
          profile.delete();
        }
      }

      final Profile profile;

      if (update.getMessage() != null) {
        final Message message = update.getMessage();

        final String chatId = String.valueOf(message.getChat().getId());
        final String userId = String.valueOf(message.getFrom().getId());

        profile = getProfileStorage()
            .query()
            .where(property("telegram", "id").eq(userId))
            .getOrCreate();

        profile.property("telegram-chats", token).set(chatId);

      } else if (update.getCallbackQuery() != null) {
        final CallbackQuery callbackQuery = update.getCallbackQuery();

        final String userId = String.valueOf(callbackQuery.getFrom().getId());
        profile = getProfileStorage()
            .query()
            .where(property("telegram", "id").eq(userId))
            .getOrCreate();

        if (callbackQuery.getMessage() != null) {
          final String chatId = String.valueOf(callbackQuery.getMessage().getChat().getId());
          profile.property("telegram-chats", token).set(chatId);
        }

      } else {
        profile = null;
      }

      req.setProfile(profile);

      //noinspection ConstantConditions
      return profile.getWnumber();
    }

    @Override
    protected String getServiceId(TelegramWebhookRequest req) {
      return req.getServiceId();
    }

    @Override
    protected String getGateway() {
      // Arbitrary description, passed to content Provider via headers (brief)
      return "Telegram";
    }

    @Override
    protected String getGatewayRequestDescription(TelegramWebhookRequest httpServletRequest) {
      // Arbitrary description, passed to content Provider via headers (detailed)
      return "Telegram";
    }

    @Override
    protected void fillSADSRequest(SADSRequest sadsRequest, TelegramWebhookRequest req) {
      super.fillSADSRequest(sadsRequest, req);

      try {
        handleFileUpload(sadsRequest, req);

      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }

    private void handleFileUpload(SADSRequest sadsRequest,
                                  TelegramWebhookRequest req) throws Exception {

      final Update update = req.asUpdate();

      final Message message = update.getMessage();
      if (message == null) {
        return;
      }

      // Steal contact data.

      final Contact contact = message.getContact();
      if (contact != null) {
        final Integer tgUserId = contact.getUserId();
        final String msisdn = contact.getPhoneNumber();

        if (tgUserId != null && msisdn != null) {
          // TODO: implement profile merging instead of manual property propagation.
          final Collection<Profile> profiles = getProfileStorage().query()
              .where(property("telegram", "id").eq(String.valueOf(tgUserId)))
              .list();
          for (Profile profile : profiles) {
            profile.property("mobile", "msisdn").set(normalize(msisdn));
          }
        }
      }

      // Submit uploaded files to the content service via request parameter.

      final List<AbstractInputType> mediaList = extractMedia(sadsRequest.getServiceId(), req);
      if (mediaList.size() <= 0) {
        return;
      }

      // XXX: can there be more than one attachment?
      req.setEvent(mediaList.iterator().next().asEvent());

      final String inputName;
      {
        final Session session = sadsRequest.getSession();
        final Document prevPage =
            (Document) session.getAttribute(SADSExecutor.ATTR_SESSION_PREVIOUS_PAGE);

        final Element input =
            prevPage == null ? null : prevPage.getRootElement().element("input");
        if (input != null) {
          inputName = input.attributeValue("name");

        } else {
          inputName = "bad_command";
        }
      }

      final String mediaParameter = MarshalUtils.marshal(mediaList);
      sadsRequest.getParameters().put(inputName, mediaParameter);
      sadsRequest.getParameters().put("input_type", "json");
    }

    private List<AbstractInputType> extractMedia(String serviceId,
                                                 TelegramWebhookRequest req)
        throws Exception {

      final Message message = req.asUpdate().getMessage();
      if (message == null) {
        return Collections.emptyList();
      }

      final List<AbstractInputType> mediaList = new ArrayList<>();

      final PhotoSize[] photoArray = message.getPhoto();
      if (photoArray != null && photoArray.length > 0) {
        for (PhotoSize photo : photoArray) {
          final InputFile file = new InputFile();
          file.setMediaType("photo");
          file.setUrl(getFilePath(serviceId, photo.getFileId()));
          file.setSize(photo.getFileSize());
          mediaList.add(file);
        }
      }

      final Audio audio = message.getAudio();
      if (audio != null) {
        final InputFile file = new InputFile();
        file.setMediaType("audio");
        file.setUrl(getFilePath(serviceId, audio.getFileId()));
        file.setContentType(audio.getMimeType());
        file.setSize(audio.getFileSize());
        mediaList.add(file);
      }

      final Sticker sticker = message.getSticker();
      if (sticker != null) {
        final InputFile file = new InputFile();
        file.setMediaType("sticker");
        file.setUrl(getFilePath(serviceId, sticker.getFileId()));
        mediaList.add(file);
      }

      final Video video = message.getVideo();
      if (video != null) {
        final InputFile file = new InputFile();
        file.setMediaType("video");
        file.setUrl(getFilePath(serviceId, video.getFileId()));
        mediaList.add(file);
      }

      final Voice voice = message.getVoice();
      if (voice != null) {
        final InputFile file = new InputFile();
        file.setMediaType("voice");
        file.setUrl(getFilePath(serviceId, voice.getFileId()));
        mediaList.add(file);
      }

      final com.eyelinecom.whoisd.sads2.telegram.api.types.Document document = message.getDocument();
      if (document != null) {
        final InputFile file = new InputFile();
        file.setMediaType("document");
        file.setUrl(getFilePath(serviceId, document.getFileId()));
        mediaList.add(file);
      }

      final Contact tContact = message.getContact();
      if (tContact != null) {
        final InputContact contact = new InputContact();
        contact.setMsisdn(tContact.getPhoneNumber());
        contact.setName(tContact.getFirstName() + " " + tContact.getLastName());
        if (tContact.getUserId() != null && tContact.getPhoneNumber() != null) {
          // Rely on contact data already persisted to profile storage.
          final Profile profile = getProfileStorage()
              .query()
              .where(property("telegram", "id").eq(String.valueOf(tContact.getUserId())))
              .where(property("mobile", "msisdn").eq(normalize(tContact.getPhoneNumber())))
              .get();
          if (profile != null) {
            contact.setId(profile.getWnumber());
          }
        }
        mediaList.add(contact);
      }

      final Location tLocation = message.getLocation();
      if (tLocation != null) {
        final InputLocation location = new InputLocation();
        location.setLatitude(tLocation.getLatitude());
        location.setLongitude(tLocation.getLongitude());
        mediaList.add(location);
      }

      return mediaList;
    }

    @Override
    protected Protocol getRequestProtocol(ServiceConfig config,
                                          String subscriberId,
                                          TelegramWebhookRequest httpServletRequest) {
      return TELEGRAM;
    }

    @Override
    protected String getRequestUri(ServiceConfig config,
                                   String wnumber,
                                   TelegramWebhookRequest message) throws Exception {

      final String serviceId = config.getId();
      final Update update = message.asUpdate();

      if (update.getMessage() != null) {
        return handleMessage(config, wnumber, message, serviceId, update.getMessage());

      } else if (update.getCallbackQuery() != null) {
        return handleCallbackQuery(config, wnumber, message, serviceId, update);

      } else {
        // Unsupported.
        log.warn("Unsupported update message: [" + update + "]");

        final Session session = getSessionManager(serviceId).getSession(wnumber);
        final String prevUri = (String) session.getAttribute(ATTR_SESSION_PREVIOUS_PAGE_URI);
        if (prevUri == null) {
          // No previous page means this is an initial request, thus serve the start page.
          return super.getRequestUri(config, wnumber, message);

        } else {
          final String badCommandPage =
              InitUtils.getString("bad-command-page", "", config.getAttributes());
          final String href = UrlUtils.merge(prevUri, badCommandPage);

          return UrlUtils.merge(prevUri, href);
        }
      }
    }

    private String handleCallbackQuery(ServiceConfig config,
                                       String wnumber,
                                       TelegramWebhookRequest message,
                                       String serviceId,
                                       Update update) throws Exception {

      // Incoming callback query from a callback button in an inline keyboard.
      final Session session = getSessionManager(serviceId).getSession(wnumber);

      final CallbackQuery callback = update.getCallbackQuery();

      final InlineCallbackQuery btnPayload =
          unmarshal(parse(callback.getData()), InlineCallbackQuery.class);

      String rootUri = (String) session.getAttribute(ATTR_SESSION_PREVIOUS_PAGE_URI);
      if (rootUri == null) {
        rootUri = super.getRequestUri(config, wnumber, message);
      }

      final String href = btnPayload.getCallbackUrl();
      return UrlUtils.merge(rootUri, href);
    }

    private String handleMessage(ServiceConfig config,
                                 String wnumber,
                                 TelegramWebhookRequest message,
                                 String serviceId,
                                 Message tgMessage) throws Exception {

      final String incoming = tgMessage.getText();

      Session session = getSessionManager(serviceId).getSession(wnumber);

      final ChatCommand cmd = ChatCommand.match(serviceId, incoming, TELEGRAM);
      if (cmd == INVALIDATE_SESSION) {
        // Invalidate the current session.
        session.close();
        session = getSessionManager(serviceId).getSession(wnumber);

      } else if (cmd == WHO_IS) {
        final String serviceToken = message.getServiceToken();
        final User me = getClient().getMe(serviceToken);

        getClient().sendMessage(
            session,
            serviceToken,
            message
                .getProfile()
                .property("telegram-chats", serviceToken)
                .getValue(),
            StringUtils.join(
                new String[] {
                    "Bot name: @" + me.getUserName() + ".",
                    "Token: " + serviceToken + ".",
                    "Service: " + serviceId + ".",
                    "Mobilizer instance: " + getRootUri()
                },
                "\n"
            )
        );

      } else if (cmd == SHOW_PROFILE) {
        final Profile profile = getProfileStorage().find(wnumber);
        final String serviceToken = message.getServiceToken();

        getClient().sendMessage(
            session,
            serviceToken,
            profile.property("telegram-chats", serviceToken).getValue(),
            profile.dump()
        );
      }

      final String prevUri = (String) session.getAttribute(ATTR_SESSION_PREVIOUS_PAGE_URI);
      if (prevUri == null) {
        // No previous page means this is an initial request, thus serve the start page.
        message.setEvent(new TextMessageEvent(incoming));
        return super.getRequestUri(config, wnumber, message);

      } else {
        final Document prevPage =
            (Document) session.getAttribute(SADSExecutor.ATTR_SESSION_PREVIOUS_PAGE);

        String href = null;
        String inputName = null;

        // Look for a button with a corresponding label.
        //noinspection unchecked
        for (Element e : (List<Element>) prevPage.getRootElement().elements("button")) {
          final String btnLabel = e.getTextTrim();

          if (StringUtils.equals(btnLabel, incoming)) {
            final String btnHref = e.attributeValue("href");
            href = btnHref != null ? btnHref : e.attributeValue("target");
            message.setEvent(new LinkEvent(btnLabel, prevUri));
          }
        }

        // Look for input field if any.
        if (href == null) {
          final Element input = prevPage.getRootElement().element("input");
          if (input != null) {
            href = input.attributeValue("href");
            inputName = input.attributeValue("name");
          }
        }

        // Nothing suitable to handle user input found, consider it a bad command.
        if (href == null) {
          final String badCommandPage =
              InitUtils.getString("bad-command-page", "", config.getAttributes());
          href = UrlUtils.merge(prevUri, badCommandPage);
          href = UrlUtils.addParameter(href, "bad_command", incoming);
        }

        if (message.getEvent() == null) {
          message.setEvent(new TextMessageEvent(incoming));
        }

        href = SADSUrlUtils.processUssdForm(href, StringUtils.trim(incoming));
        if (inputName != null) {
          href = UrlUtils.addParameter(href, inputName, incoming);
        }

        return UrlUtils.merge(prevUri, href);
      }
    }

    /**
     * @param request   Request to the content provider
     * @param response  Response from content provider
     */
    @Override
    protected SADSResponse getOuterResponse(TelegramWebhookRequest req,
                                            SADSRequest request,
                                            SADSResponse response) {
      return buildWebhookResponse(200);
    }

    private SADSResponse buildWebhookResponse(int statusCode) {
      return buildWebhookResponse(statusCode, "{}");
    }

    private SADSResponse buildWebhookResponse(int statusCode, String body) {
      final SADSResponse rc = new SADSResponse();
      rc.setStatus(statusCode);
      rc.setHeaders(Collections.<String, String>emptyMap());
      rc.setMimeType("application/json");
      rc.setData(body.getBytes());
      return rc;
    }

    private String normalize(String msisdn) {
      if (msisdn == null) {
        return null;
      }

      msisdn = msisdn.replaceAll("[^0-9]+", "");
      if (msisdn.length() < 11) {
        return null;
      }

      if (msisdn.length() == 11 && msisdn.startsWith("8")) {
        msisdn = msisdn.replaceFirst("8", "7");
      }

      return msisdn;
    }

    @Override
    protected Profile getCachedProfile(TelegramWebhookRequest req) {
      return req.getProfile();
    }

    @Override
    protected Event getEvent(TelegramWebhookRequest req) {
      return req.getEvent();
    }

    private String getFilePath(String serviceId, String fileId) {

      // Note: using absolute URL here seems redundant (and might cause a lot of issues),
      // but we don't have any way to inform content provider of our actual URL.
      //
      // Consider using headers for this, like always passing "X-MyHostName = ...".
      return getRootUri() + "/files/" +  serviceId + "/telegram/" + fileId;
    }

    private TelegramApi getClient() throws NotFoundResourceException {
      return (TelegramApi) getResource("telegram-api");
    }

    private SessionManager getSessionManager(String serviceId) throws Exception {
      return getSessionManager(TELEGRAM, serviceId);
    }

  }

}
