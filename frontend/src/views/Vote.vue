<template>
  <div class="home">
    <b-navbar>
      <template slot="brand">
        <b-navbar-item tag="router-link" :to="{ path: '/' }">
          <p class="is-size-4">Strip Poker</p>
        </b-navbar-item>
      </template>
      <template slot="start">
        <b-navbar-item href="/"> Go back </b-navbar-item>
        <b-navbar-item @click="copyUrlToClipboard($event)">
          Copy URL
        </b-navbar-item>
      </template>
    </b-navbar>

    <section v-if="connected">
      <Board
        :users="users"
        :yourId="yourId"
        :yourVote="yourVote"
        :leaderId="leaderId"
        :revealed="revealed"
        @selectvote="selectVote"
        @sendreveal="sendReveal"
        @sendreset="sendReset"
      >
      </Board>
    </section>

    <section v-else>
      <div class="container">
        <h1>Connecting</h1>
      </div>
    </section>
  </div>
</template>

<script lang='ts'>
import { Component, Vue } from "vue-property-decorator";
import Board from "../components/Board.vue";
import { VoteValue, voteValues, UserStatus } from "../model";

@Component({
  components: {
    Board,
  },
})
export default class Home extends Vue {
  private connected = false;

  private voteValues = voteValues;

  private users: UserStatus[] = [];
  private yourId = "";
  private leaderId = "";
  private revealed = false;

  private yourVote?: VoteValue = VoteValue.pending;

  private ws?: WebSocket;

  public mounted() {
    this.connected = false;
    this.revealed = false;

    const roomId = this.$route.params.roomid;
    const scheme = window.location.protocol === "https:" ? "wss:" : "ws:";
    this.ws = new WebSocket(
      `${scheme}//${window.location.host}/api/voteconnection/${roomId}`
    );

    if (this.ws) {
      this.ws.addEventListener("open", (connectionEvent) => {
        this.connected = true;

        this.ws!.addEventListener("message", (event) => {
          this.handleMessage(event);
        });

        this.ws!.addEventListener("close", (event) => {
          if (event.reason) {
            localStorage.flash = event.reason;
          }

          this.$router.push({ name: "SelectRoom" });
        });
      });
    }
  }

  public selectVote(voteValue: VoteValue) {
    if (this.revealed == false) {
      this.yourVote = voteValue;

      const command = JSON.stringify({
        messagetype: "selectvote",
        vote: voteValue.toString(),
      });

      this.sendCommand(command);
    }
  }

  public sendReveal(voteValue: VoteValue) {
    const command = JSON.stringify({ messagetype: "sendreveal" });

    this.sendCommand(command);
  }

  public sendReset(voteValue: VoteValue) {
    this.yourVote = undefined;

    const command = JSON.stringify({ messagetype: "sendreset" });

    this.sendCommand(command);
  }

  private handleMessage(event: MessageEvent) {
    const msg = JSON.parse(event.data);

    if (msg.messagetype === "userstatus") {
      this.users = msg.users;
      this.yourId = msg.yourId;
      this.yourVote = msg.yourVote;
      this.leaderId = msg.leaderId;
      this.revealed = msg.revealed;
    }
  }

  private sendCommand(command: any) {
    this.ws!.send(command);
  }

  private copyUrlToClipboard(e: Event) {
    e.preventDefault();
    navigator.clipboard.writeText(window.location.href);
  }
}
</script>


<style scoped>
.home {
  max-width: 900px;
  margin: 0 auto;
}
</style>