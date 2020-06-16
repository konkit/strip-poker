module.exports = {
  chainWebpack: config => {
    config
      .plugin('html')
      .tap(args => {
        args[0].title = 'Strip Poker'
        return args
      })
  },

  devServer: {
    proxy: {
      '^/api': {
        target: "http://localhost:9999",
        ws: true,
        changeOrigin: true
      },
    }
  },
}