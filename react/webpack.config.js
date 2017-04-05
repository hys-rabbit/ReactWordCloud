module.exports = {
  entry: __dirname + '/src/index.js',
  output: {
    path: __dirname + '/../play/public/javascripts',
    filename: 'bundle.js'
  },

  module: {
    loaders: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        loader: 'babel-loader',
        query: {
          presets: ['react', 'es2015', 'stage-1']
        }
      },
      {
        test: /\.css$/,
        loader: 'style-loader!css-loader'
      }
    ]
  },
  devtool: ['source-map', 'inline-source-map', 'eval-source-map'],
  resolve: {
    extensions: ['.js', '.jsx', '.css']
  }
}
