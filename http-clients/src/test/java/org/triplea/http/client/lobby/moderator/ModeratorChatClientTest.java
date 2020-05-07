package org.triplea.http.client.lobby.moderator;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.triplea.http.client.HttpClientTesting.EXPECTED_API_KEY;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.triplea.domain.data.PlayerChatId;
import org.triplea.http.client.AuthenticationHeaders;
import org.triplea.http.client.WireMockTest;
import ru.lanwen.wiremock.ext.WiremockResolver;

class ModeratorChatClientTest extends WireMockTest {
  private static final BanPlayerRequest BAN_PLAYER_REQUEST =
      BanPlayerRequest.builder()
          .playerChatId(PlayerChatId.of("chat-id").getValue())
          .banMinutes(20)
          .build();
  private static final PlayerChatId PLAYER_CHAT_ID = PlayerChatId.of("player-chat-id");

  private static final PlayerSummaryForModerator PLAYER_SUMMARY_FOR_MODERATOR =
      PlayerSummaryForModerator.builder()
          .name("name")
          .systemId("system-id")
          .ip("5.5.3.3")
          .aliases(List.of())
          .bans(
              List.of(
                  PlayerSummaryForModerator.BanInformation.builder()
                      .epochMillEndDate(1000)
                      .epochMilliStartDate(2000)
                      .name("name-banned")
                      .systemId("id")
                      .ip("ip")
                      .build()))
          .build();

  private static final ChatHistoryMessage CHAT_HISTORY_MESSAGE =
      ChatHistoryMessage.builder()
          .username("chatter")
          .message("Message")
          .epochMilliDate(6000)
          .build();

  private static ModeratorChatClient newClient(final WireMockServer wireMockServer) {
    return newClient(wireMockServer, ModeratorChatClient::newClient);
  }

  @Test
  void banPlayer(@WiremockResolver.Wiremock final WireMockServer server) {
    server.stubFor(
        WireMock.post(ModeratorChatClient.BAN_PLAYER_PATH)
            .withHeader(AuthenticationHeaders.API_KEY_HEADER, equalTo(EXPECTED_API_KEY))
            .withRequestBody(equalToJson(toJson(BAN_PLAYER_REQUEST)))
            .willReturn(WireMock.aResponse().withStatus(200)));

    newClient(server).banPlayer(BAN_PLAYER_REQUEST);
  }

  @Test
  void disconnectPlayer(@WiremockResolver.Wiremock final WireMockServer server) {
    server.stubFor(
        WireMock.post(ModeratorChatClient.DISCONNECT_PLAYER_PATH)
            .withHeader(AuthenticationHeaders.API_KEY_HEADER, equalTo(EXPECTED_API_KEY))
            .withRequestBody(equalTo(PLAYER_CHAT_ID.getValue()))
            .willReturn(WireMock.aResponse().withStatus(200)));

    newClient(server).disconnectPlayer(PLAYER_CHAT_ID);
  }

  @Test
  void fetchPlayerInfo(@WiremockResolver.Wiremock final WireMockServer server) {
    server.stubFor(
        WireMock.post(ModeratorChatClient.FETCH_PLAYER_INFORMATION)
            .withHeader(AuthenticationHeaders.API_KEY_HEADER, equalTo(EXPECTED_API_KEY))
            .withRequestBody(equalTo(PLAYER_CHAT_ID.getValue()))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(toJson(PLAYER_SUMMARY_FOR_MODERATOR))));

    final var result = newClient(server).fetchPlayerInformation(PLAYER_CHAT_ID);
    assertThat(result, is(PLAYER_SUMMARY_FOR_MODERATOR));
  }

  @Test
  void fetchGameChatHistory(@WiremockResolver.Wiremock final WireMockServer server) {
    server.stubFor(
        WireMock.post(ModeratorChatClient.FETCH_GAME_CHAT_HISTORY)
            .withHeader(AuthenticationHeaders.API_KEY_HEADER, equalTo(EXPECTED_API_KEY))
            .withRequestBody(equalTo("game-id"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(toJson(List.of(CHAT_HISTORY_MESSAGE)))));

    final List<ChatHistoryMessage> chats = newClient(server).fetchChatHistoryForGame("game-id");

    assertThat(chats.get(0), is(CHAT_HISTORY_MESSAGE));
  }
}
