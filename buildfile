require 'buildr/git_auto_version'
require 'buildr/gwt'
require 'reality/naming'

GWT_MODULES = %w(Tennis Breakout Racing Adventure)

desc 'The How to Program Games using HTML5 Canvas coursework'
define 'course-arcade-games' do
  project.group = 'org.realityforge.arcade'
  compile.options.source = '1.8'
  compile.options.target = '1.8'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  compile.with :javax_annotation,
               :jsinterop_base,
               :jsinterop_annotations,
               :elemental2_promise,
               :elemental2_core,
               :elemental2_dom,
               :braincheck,
               :gwt_user

  gwt_config = {}
  GWT_MODULES.each do |m|
    uname = Reality::Naming.underscore(m)
    module_name = "org.realityforge.arcade.#{uname}.#{m}"
    gwt_config[module_name] = false
    gwt([module_name],
        {
          :java_args => %w(-Xms512M -Xmx1024M -Dgwt.watchFileChanges=false),
          :dependencies => project.compile.dependencies + [project.compile.target] + [Buildr.artifact(:gwt_user)],
          :gwtc_args => %w(-optimize 9 -checkAssertions -XmethodNameDisplayMode FULL -noincremental),
          :output_key => uname
        })
    ipr.add_gwt_configuration(project,
                              :gwt_module => module_name,
                              :start_javascript_debugger => false,
                              :open_in_browser => false,
                              :vm_parameters => '-Xmx2G',
                              :shell_parameters => "-style PRETTY -XmethodNameDisplayMode FULL -noincremental -port 8888 -codeServerPort 8889 -bindAddress 0.0.0.0 -war #{_(:generated, uname, 'gwt-export')}/",
                              :launch_page => "http://127.0.0.1:8888/#{uname}/")
  end

  project.iml.add_gwt_facet(gwt_config, :settings => {
    :compilerMaxHeapSize => '1024',
    :compilerParameters => '-draftCompile -localWorkers 2 -strict'
  }, :gwt_dev_artifact => :gwt_dev)

  iml.excluded_directories << project._('tmp')

  ipr.add_component_from_artifact(:idea_codestyle)
  ipr.add_component('JavaProjectCodeInsightSettings') do |xml|
    xml.tag!('excluded-names') do
      xml << '<name>com.sun.istack.internal.NotNull</name>'
      xml << '<name>com.sun.istack.internal.Nullable</name>'
      xml << '<name>org.jetbrains.annotations.Nullable</name>'
      xml << '<name>org.jetbrains.annotations.NotNull</name>'
      xml << '<name>org.testng.AssertJUnit</name>'
    end
  end
  ipr.add_component('NullableNotNullManager') do |component|
    component.option :name => 'myDefaultNullable', :value => 'javax.annotation.Nullable'
    component.option :name => 'myDefaultNotNull', :value => 'javax.annotation.Nonnull'
    component.option :name => 'myNullables' do |option|
      option.value do |value|
        value.list :size => '2' do |list|
          list.item :index => '0', :class => 'java.lang.String', :itemvalue => 'org.jetbrains.annotations.Nullable'
          list.item :index => '1', :class => 'java.lang.String', :itemvalue => 'javax.annotation.Nullable'
        end
      end
    end
    component.option :name => 'myNotNulls' do |option|
      option.value do |value|
        value.list :size => '2' do |list|
          list.item :index => '0', :class => 'java.lang.String', :itemvalue => 'org.jetbrains.annotations.NotNull'
          list.item :index => '1', :class => 'java.lang.String', :itemvalue => 'javax.annotation.Nonnull'
        end
      end
    end
  end
end
