<template>
  <div class="home">
    <nav class="navbar" role="navigation" aria-label="main navigation">
      <div class="navbar-brand">
        <a class="navbar-item" href="/">
          <p class="is-size-4">Strip Poker</p>
        </a>

        <a
          role="button"
          class="navbar-burger burger"
          aria-label="menu"
          aria-expanded="false"
          data-target="navbarBasicExample"
        >
          <span aria-hidden="true"></span>
          <span aria-hidden="true"></span>
          <span aria-hidden="true"></span>
        </a>

        <div class="navbar-menu">
          <div class="navbar-start">
            <a class="navbar-item" href="/">Go back</a>
          </div>
        </div>
      </div>
    </nav>

    <section v-if="connected">
      <Board 
        :users="users" 
        :yourId="yourId" 
        :yourVote="yourVote"
        :leaderId="leaderId" 
        :revealed="revealed" 
        @selectvote="selectVote"
        @sendreveal="sendReveal"
        @sendreset="sendReset">
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
import {VoteValue, voteValues, UserStatus} from "../model"

@Component({
  components: {
    Board
  }
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
    this.ws = new WebSocket(`ws://${window.location.host}/api/voteconnection/${roomId}`);

    if (this.ws) {
      this.ws.addEventListener("open", connectionEvent => {
        this.connected = true;

        this.ws!.addEventListener("message", event => {
          this.handleMessage(event);
        });

        this.ws!.addEventListener("close", event => {
          if (event.reason) {
            localStorage.flash = event.reason;
          }

          this.$router.push({ name: "SelectRoom" });
        });
      });
    }
  }

  public selectVote(voteValue: VoteValue) {
    this.yourVote = voteValue;

    const command = JSON.stringify({
      messagetype: "selectvote",
      vote: voteValue.toString()
    });

    this.sendCommand(command);
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
}
</script>


<style scoped>
</style>