<template>
  <div class='selectroom'>
    <h1>Strip Poker</h1>
    <h2>simple planning poker app with T-shirt size estimates and an inappropriate name</h2>

    <div v-if='flashMsg'>
      {{flashMsg}}
    </div>

    <div>
      <h3>Enter room number</h3>

      <input type='text' v-model='roomNumber' />
      <button @click='joinRoom()'>Join</button>
      <button @click='createRoom()'>Create room</button>
    </div>
  </div>
</template>

<script lang='ts'>
import { Component, Vue } from 'vue-property-decorator';
import axios from 'axios';

@Component
export default class SelectRoom extends Vue {
  public roomNumber?: string = '';

  public flashMsg: string = '';

  constructor() {
    super();
  }

  public mounted() {
    if (localStorage.flash) {
      this.flashMsg = localStorage.flash;
      localStorage.flash = '';
    }
  }

  public joinRoom() {
    if (this.roomNumber) {
      this.$router.push({name: 'Vote', params: {roomid: this.roomNumber}});
    }
  }

  public createRoom() {
    const ws = new WebSocket('ws://localhost:9999/createroom');

    ws.addEventListener('message', (event) => {
      this.$router.push({name: 'Vote', params: {roomid: event.data}});
    });
  }
}
</script>