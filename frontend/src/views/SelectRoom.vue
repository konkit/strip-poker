<template>
  <div class="about">
    <h1>This is an about page</h1>

    <div>
      <h1>Select Room</h1>

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