<template>
  <div class="container">
    <h1 v-if="revealed" class="title is-1 has-text-centered">Voting complete!</h1>
    <h1 v-else class="title is-1 has-text-centered">Waiting for votes ...</h1>

    <div class="container">
      <transition-group name="fade" class="cast-votes">
      <div
        v-for="(user, i) in users"
        class="vote is-size-3"
        :class="{'your-vote': yourVote && user.id === yourId}"
        :key="i"
      >
          <span v-if="revealed">{{user.vote}}</span>
      </div>
      </transition-group>
    </div>

    <div class="container select-vote-wrapper has-text-centered">
      <h3 v-if="!revealed" class="title is-3">Select your estimate</h3>
      <h3 v-if="revealed && isLeader()" class="title is-4">Press the button below to start a new vote</h3>
      <h3 v-if="revealed && !isLeader()" class="title is-4">Please wait until the host starts a new vote</h3>

      <div class="votes-to-select">
        <div
          class="vote"
          :class="{'vote-selected': voteValue === yourVote, 'selectable': revealed === false}"
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
      <button v-if="revealed" class="button is-success" @click="sendReset()">New Vote</button>
      <button v-if="!revealed" class="button" @click="sendReset()">Reset</button>
    </div>
  </div>
</template>

<script lang='ts'>
import { Component, Vue, Prop } from "vue-property-decorator";
import { VoteValue, voteValues, UserStatus } from "../model";

@Component
export default class Board extends Vue {
  public voteValues = voteValues;

  @Prop() private users?: UserStatus[];
  @Prop() private yourId?: string;
  @Prop() private yourVote?: VoteValue;
  @Prop() private leaderId?: string;
  @Prop() private revealed?: boolean;

  public mounted() {
  }

  public selectVote(voteValue: VoteValue) {
    this.$emit("selectvote", voteValue);
  }

  public sendReveal() {
    this.$emit("sendreveal");
  }

  public sendReset() {
    this.$emit("sendreset");
  }

  public isLeader() {
    return this.leaderId === this.yourId
  }
}
</script>


<style scoped>
.cast-votes {
  display: flex;
  flex-direction: row;
  justify-content: center;
  margin-top: 30px;
  flex-wrap: wrap;
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
  display: flex;
  flex-direction: row;
  justify-content: center;
}

.vote {
  margin: 10px;

  width: 100px;
  min-width: 100px;

  height: 160px;
  min-height: 160px;

  text-align: center;
  border: 1px solid #aaa;
  border-radius: 5px;
  display: flex;
  flex-direction: column;
  align-content: center;
  justify-content: center;
  cursor: default;
}

.vote.selectable:hover {
  cursor: pointer;
  background-color: #f5f5f5;
}

.vote-selected {
  border: 5px solid black;
}

.your-vote {
  border: 5px solid black;
}

.leader-buttons {
  margin-top: 20px;
}

.leader-buttons button {
  margin: 0 10px;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 1.5s;
}
.fade-enter, .fade-leave-to /* .fade-leave-active below version 2.1.8 */ {
  opacity: 0;
}
</style>