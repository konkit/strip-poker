<template>
  <div class='home'>
    <div v-if='connected'>
      <h1>Voting - {{ revealed ? 'complete' : 'pending' }}</h1>

      <div class='cast-votes'>
        <div
          v-for="(user, i) in users"
          class="cast-vote"
          :class="{'your-vote': user.id === yourId}"
          :key="i"
        >{{user.vote}}</div>
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

<script lang='ts'>
import { Component, Vue } from 'vue-property-decorator';
import HelloWorld from './components/HelloWorld.vue';

enum VotingState {
  pending = 'pending',
  revealed = 'revealed',
}

enum VoteValue {
  noVote = 'NO',
  dunno = '?',
  hidden = 'x',
  small = 'S',
  medium = 'M',
  large = 'L',
}

@Component
export default class Home extends Vue {
  public connected = false;

  public voteValues = ['S', 'M', 'L'];

  public users = [];
  public yourId = '';
  public leaderId = '';
  public revealed = false;

  public state = VotingState.pending;

  public yourVote?: VoteValue;

  private ws?: WebSocket;

  public mounted() {
    this.connected = false;
    this.revealed = false;

    const roomId = this.$route.params.roomid;
    this.ws = new WebSocket('ws://localhost:9999/voteconnection/' + roomId);

    if (this.ws) {
      this.ws.addEventListener('open', (connectionEvent) => {
        this.connected = true;

        this.ws!.addEventListener('message', (event) => {
          this.handleMessage(event);
        });

        this.ws!.addEventListener('close', (event) => {
          if (event.reason) {
            localStorage.flash = event.reason;
          }

          this.$router.push({ name: 'SelectRoom' });
        });
      });
    }
  }

  public selectVote(voteValue: VoteValue) {
    this.yourVote = voteValue;

    const command = JSON.stringify({
      messagetype: 'selectvote',
      vote: voteValue.toString(),
    });

    this.sendCommand(command);
  }

  public sendReveal(voteValue: VoteValue) {
    const command = JSON.stringify({ messagetype: 'sendreveal' });

    this.sendCommand(command);
  }

  public sendReset(voteValue: VoteValue) {
    this.yourVote = undefined;

    const command = JSON.stringify({ messagetype: 'sendreset' });

    this.sendCommand(command);
  }

   private handleMessage(event: MessageEvent) {
    const msg = JSON.parse(event.data);

    if (msg.messagetype === 'userstatus') {
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