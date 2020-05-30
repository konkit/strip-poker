<template>
  <div class="home">
    <h1>Voting - {{state}}</h1>

    <div class="cast-votes">
      <div v-for="(user, i) in users" class="cast-vote" :class="{'your-vote': user.id === yourId}" :key="i">
        {{user.vote}}
      </div>
    </div>

    <div class="select-vote-wrapper">
      <h3>Select vote</h3>

      <div class="votes-to-select">
        <div class="vote" :class="{'vote-selected': voteValue === yourVote}" v-for="(voteValue, i) in voteValues" :key="i" @click="selectVote(voteValue)">
          <div>{{voteValue}}</div>
        </div>
      </div>
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
  voteValues = ["S", "M", "L"];

  yourId = ""
  users = []

  state = VotingState.pending

  yourVote = null

  ws = null

  selectVote(voteValue: VoteValue) {
    this.yourVote = voteValue

    let command = JSON.stringify({messagetype: "selectvote", id: this.yourId, vote: voteValue.toString()})

    console.log("Command to be sent: ", command)

    this.ws.send(command)
  }

  mounted() {
    this.ws = new WebSocket("ws://localhost:9999/myws")

    // Connection opened
    this.ws.addEventListener('open', function (event) {
        // this.ws.send('Hello Server!');
    });

    // Listen for messages
    this.ws.addEventListener('message', (event) => {
      console.log('Message from server ', event.data);

      let msg = JSON.parse(event.data)

      if (msg.messagetype == 'userstatus') {
        this.users = msg.users
        this.yourId = msg.you
      }

    });
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