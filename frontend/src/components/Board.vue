<template>
  <div class="container">
    <h1 v-if="revealed" class="title is-1 has-text-centered">Voting complete!</h1>
    <h1 v-else class="title is-1 has-text-centered">Waiting for votes ...</h1>

    <div class="container cast-votes">
      <div
        v-for="(user, i) in users"
        class="cast-vote is-size-3"
        :class="{'your-vote': user.id === yourId}"
        :key="i"
      >{{user.vote}}</div>
    </div>

    <div class="container select-vote-wrapper has-text-centered">
      <h3 class="title is-3">Select your estimate</h3>

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
    this.yourVote = voteValue;

    this.$emit("selectvote", voteValue);
  }

  public sendReveal() {
    this.$emit("sendreveal");
  }

  public sendReset() {
    this.$emit("sendreset");
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