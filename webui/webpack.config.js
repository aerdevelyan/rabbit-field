const path = require('path');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
    mode: 'development',
	entry: './src/main/js/app.js',	
	output: {
		filename: 'bundle.js',
		path: path.resolve(__dirname, 'target/dist')
	},
	module: {
		rules: [ 
			{
			    test: /\.css$/,
			    use: [ 'style-loader', 'css-loader' ]
			}, {
			    test: /\.(png|svg|jpg|gif)$/,
			    use: [ 'file-loader' ]
			} 
		]
	},
	plugins: [
	    new CopyWebpackPlugin([
	        { from: 'src/main/*.html', flatten: true }
	    ])
	]
};
