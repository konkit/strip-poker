<template>
  <div class="selectroom">
    <h1>Strip Poker</h1>
    <h2>simple planning poker app with T-shirt size estimates and an inappropriate name</h2>

    <div>
      <h3>Enter room number</h3>

      <input type="text" v-model="roomNumber" />
      <button @click="joinRoom()">Join</button>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";

@Component
export default class SelectRoom extends Vue {
  roomNumber = null

  constructor() {
    super();
  }

  joinRoom() {
    this.$router.push({name: "Vote", params: {roomid: this.roomNumber}})
  }
}
</script>