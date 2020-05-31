import Vue from 'vue';
import VueRouter, { RouteConfig } from 'vue-router';
import SelectRoom from '../views/SelectRoom.vue';
import Vote from '../views/Vote.vue';

Vue.use(VueRouter);

const routes: RouteConfig[] = [
  {
    path: '/',
    name: 'SelectRoom',
    component: SelectRoom,
  },
  {
    path: '/vote/:roomid',
    name: 'Vote',
    component: Vote,
  },
];

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes,
});

export default router;
