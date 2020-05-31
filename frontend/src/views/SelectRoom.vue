<template>
  <div class="selectroom">
    <h1>Strip Poker</h1>
    <h2>simple planning poker app with T-shirt size estimates and an inappropriate name</h2>

    <div>
      <h3>Enter room number</h3>

      <input type="text" v-model="roomNumber" />
      <button @click="joinRoom()">Join</button>
      <button @click="createRoom()">Create room</button>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import axios from "axios"

@Component
export default class SelectRoom extends Vue {
  roomNumber = null

  constructor() {
    super();
  }

  mounted() {
    axios
      .get("http://localhost:9999/")
      .then(response => console.log(response.data))
  }

  joinRoom() {
    this.$router.push({name: "Vote", params: {roomid: this.roomNumber}})
  }

  createRoom() {
    // axios
    //   .post("http://localhost:9999/createroom", {})
    //   .then(response => this.$router.push({name: "Vote", params: {roomid: response.data}}))

    let ws = new WebSocket("ws://localhost:9999/createroom");

    ws.addEventListener("message", event => { 
      this.$router.push({name: "Vote", params: {roomid: event.data}})
    });
  }
}
</script>