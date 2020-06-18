<template>
  <div class="selectroom">
    <section class="hero is-fullheight is-primary is-bold">
      <div class="hero-body">
        <div class="container has-text-centered">
          <h1 class="title is-size-1 has-text-weight-bold">Strip Poker</h1>
          <h2 class="subtitle is-3">
            A simple
            <strong>planning poker</strong> app with
            <strong>T-shirt size estimates</strong> and an inappropriate name
          </h2>

          <div class="has-margin-top-20" v-if="flashMsg">{{flashMsg}}</div>

          <div class="columns is-vcentered has-margin-top-20">
            <div class="column is-6">
              <p class="subtitle is-4">Create a new room...</p>
              <button class="button is-large is-light" @click="createRoom()">Create a room</button>
            </div>

            <div class="column is-6">
              <p class="subtitle is-4">... or join an existing one</p>

              <div class="field has-addons has-addons-centered">
                <div class="control">
                  <input type="text" class="input is-large" v-model="roomNumber" />
                </div>
                <div class="control">
                  <button class="button is-large is-light" @click="joinRoom()">Join a room</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script lang='ts'>
import { Component, Vue } from "vue-property-decorator";
import axios from "axios";

@Component
export default class SelectRoom extends Vue {
  public roomNumber?: string = "";

  public flashMsg: string = "";

  constructor() {
    super();
  }

  public mounted() {
    if (localStorage.flash) {
      this.$buefy.snackbar.open({
          duration: 5000,
          message: localStorage.flash,
          type: 'is-danger',
          position: 'is-bottom-left',
          queue: false,
      })
      localStorage.flash = "";
    }
  }

  public joinRoom() {
    if (this.roomNumber) {
      this.$router.push({ name: "Vote", params: { roomid: this.roomNumber } });
    }
  }

  public createRoom() {
    const ws = new WebSocket(`ws://${window.location.host}/api/createroom`);

    ws.addEventListener("message", event => {
      this.$router.push({ name: "Vote", params: { roomid: event.data } });
    });
  }
}
</script>

<style scoped>
.room-number-input {
  width: 100px;
}

.has-margin-top-20 {
  margin-top: 20px;
}

.hero {
  background-image: url("~@/assets/cover3.jpg") !important;
  -webkit-background-size: cover;
  -moz-background-size: cover;
  -o-background-size: cover;
  background-size: cover;
}
</style>