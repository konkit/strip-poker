import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import Buefy from 'buefy';
import 'buefy/dist/buefy.css';

import * as Sentry from '@sentry/browser';
import { Vue as VueIntegration } from '@sentry/integrations';

Vue.config.productionTip = false;

Vue.use(Buefy);

if (process.env.NODE_ENV === 'production') {
  Sentry.init({
    dsn: 'https://1761ba23418845ce91aeead31f914028@o245698.ingest.sentry.io/5278976',
    integrations: [new VueIntegration({Vue, attachProps: true})],
  });
}


new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount('#app');
