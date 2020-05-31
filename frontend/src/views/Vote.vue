<template>
  <div class="home">
    <div v-if="connected">
      <h1>Voting - {{ revealed ? "complete" : "pending" }}</h1>

      <div class="cast-votes">
        <div
          v-for="(user, i) in users"
          class="cast-vote"
          :class="{'your-vote': user.id === yourId}"
          :key="i"
        >
          {{user.vote}}
        </div>
      </div>

      <div class="select-vote-wrapper">
        <h3>Select vote</h3>

        <div class="votes-to-select">
          <div
            class="vote"
            :class="{'vote-selected': voteValue === yourVote}"
            v-for="(voteValue, i) in voteValues"
            :key="i"
            @click="selectVote(voteValue)"
          >
            <div>{{voteValue}}</div>
          </div>
        </div>
      </div>

      <div v-if="leaderId == yourId">
        <button @click="sendReveal()" :disabled="revealed">Reveal</button>
        <button @click="sendReset()">Reset</button>
      </div>
    </div>
    <div v-else>
      <h1>Connecting</h1>
    </div>
  </div>
</template>

<script lang="ts">
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
  connected = false;

  voteValues = ["S", "M", "L"];

  users = [];
  yourId = "";
  leaderId = "";
  revealed = false;

  state = VotingState.pending;

  yourVote = null;

  ws = null;



  mounted() {
    this.connected = false
    this.revealed = false

    const roomId = this.$route.params.roomid
    this.ws = new WebSocket("ws://localhost:9999/voteconnection/" + roomId);

    this.ws.addEventListener("open", event => {
      this.connected = true;

      this.ws.addEventListener("message", event => {
        this.handleMessage(event)
      });

      this.ws.addEventListener("close", event => {
        this.$router.push({name: "SelectRoom"})
      });
    });
  }

  handleMessage(event) {
    console.log("Message from server ", event.data);

    let msg = JSON.parse(event.data);

    if (msg.messagetype == "userstatus") {
      this.users = msg.users;
      this.yourId = msg.yourId;
      this.yourVote = msg.yourVote;
      this.leaderId = msg.leaderId;
      this.revealed = msg.revealed;
    }
  }

  selectVote(voteValue: VoteValue) {
    this.yourVote = voteValue;

    let command = JSON.stringify({messagetype: "selectvote", vote: voteValue.toString()});

    this.sendCommand(command);
  }

  sendReveal(voteValue: VoteValue) {
    let command = JSON.stringify({messagetype: "sendreveal"});

    this.sendCommand(command);
  }

  sendReset(voteValue: VoteValue) {
    this.yourVote = null;

    let command = JSON.stringify({ messagetype: "sendreset" });

    this.sendCommand(command);
  }

  sendCommand(command) {
    console.log("Command to be sent: ", command);
    this.ws.send(command);
  }
}
</script>


<style scoped>
.cast-votes {
  display: flex;
  flex-direction: row;
  justify-content: space-around;
}

.cast-vote {
  padding: 50px 50px;
  border: 1px solid black;
}

.your-vote {
  border: 5px solid black;
}

.select-vote-wrapper {
  display: flex;
  flex-direction: column;
}

.votes-to-select {
  display: flex;
  flex-direction: row;
  width: 100%;
  justify-content: space-around;
}

.vote {
  padding: 50px 50px;
  border: 1px solid black;
  cursor: pointer;
}

.vote-selected {
  border: 5px solid black;
}
</style>