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

        <div id="navbarBasicExample" class="navbar-menu">
          <div class="navbar-start">
            <a class="navbar-item" href="/">Go back</a>
          </div>
        </div>
      </div>
    </nav>

    <section>
      <v-container v-if="connected">
        <h1 v-if="revealed" class="title is-1 has-text-centered">
          Voting complete!
        </h1>
        <h1 v-else class="title is-1 has-text-centered">
          Waiting for votes ...
        </h1>

        <div class="container cast-votes">
          <div
            v-for="(user, i) in users"
            class="cast-vote is-size-3"
            :class="{'your-vote': user.id === yourId}"
            :key="i"
          >{{user.vote}}</div>
        </div>

        <div class="container select-vote-wrapper has-text-centered">
          <h3 class="title is-3">Select your vote</h3>

          <div class="votes-to-select">
            <div
              class="vote"
              :class="{'vote-selected': voteValue === yourVote}"
              v-for="(voteValue, i) in voteValues"
              :key="i"
              @click="selectVote(voteValue)"
            >
              <div class="is-size-3">{{voteValue}}</div>
            </div>
          </div>
        </div>

        <div v-if="leaderId == yourId" class="container leader-buttons has-text-centered">
          <button class="button is-primary" @click="sendReveal()" :disabled="revealed">Reveal</button>
          <button class="button" @click="sendReset()">Reset</button>
        </div>
      </v-container>
      <v-container v-else>
        <h1>Connecting</h1>
      </v-container>
    </section>
  </div>
</template>

<script lang='ts'>
import { Component, Vue } from "vue-property-decorator";
import HelloWorld from "./components/HelloWorld.vue";

enum VotingState {
  pending = "pending",
  revealed = "revealed"
}

enum VoteValue {
  noVote = "NO",
  dunno = "?",
  hidden = "x",
  small = "S",
  medium = "M",
  large = "L"
}

@Component
export default class Home extends Vue {
  public connected = false;

  public voteValues = ["S", "M", "L"];

  public users = [];
  public yourId = "";
  public leaderId = "";
  public revealed = false;

  public state = VotingState.pending;

  public yourVote?: VoteValue;

  private ws?: WebSocket;

  public mounted() {
    this.connected = false;
    this.revealed = false;

    const roomId = this.$route.params.roomid;
    this.ws = new WebSocket("ws://localhost:9999/voteconnection/" + roomId);

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
.cast-votes {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  margin-top: 30px;
}

.cast-vote {
  width: 100px;
  height: 100px;
  text-align: center;
  border: 1px solid black;
  display: flex;
  flex-direction: column;
  align-content: center;
  justify-content: center;
}

.your-vote {
  border: 5px solid black;
}

.select-vote-wrapper {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.votes-to-select {
  width: 500px;
  display: flex;
  flex-direction: row;
  justify-content: space-around;
}

.vote {
  width: 100px;
  height: 100px;
  text-align: center;
  border: 1px solid black;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-content: center;
  justify-content: center;
}

.vote-selected {
  border: 5px solid black;
}

.leader-buttons {
  margin-top: 20px;
}
</style>