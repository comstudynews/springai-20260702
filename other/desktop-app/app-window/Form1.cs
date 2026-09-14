namespace app_window;

using Microsoft.Web.WebView2.WinForms;

public partial class Form1 : Form
{
    private WebView2 webView;

    public Form1()
    {
        InitializeComponent();
        this.Text = "WebView2 브라우저 예제";
        this.Width = 1200;
        this.Height = 800;

        webView = new WebView2
        {
            Dock = DockStyle.Fill,
        };

        this.Controls.Add(webView);
        this.Load += MainForm_Load;        
    }

    private async void MainForm_Load(object sender, EventArgs e)
    {
        // WebView2 초기화
        await webView.EnsureCoreWebView2Async(null);
        webView.CoreWebView2.Navigate("http://localhost:8080");
    }    
}
